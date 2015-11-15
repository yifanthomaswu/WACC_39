package wacc.visitor;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.*;
import antlr.BasicParser.*;
import wacc.symboltable.SymbolTable;
import wacc.visitor.utils.*;

public class SemanticVisitor extends BasicParserBaseVisitor<Void> {

  private SymbolTable st;

  @Override
  public Void visitProgram(ProgramContext ctx) {
    SymbolTable globalTable = new SymbolTable(null);
    st = globalTable;
    for (FuncContext func : ctx.func()) {
      String ident = func.ident().getText();
      if (st.lookup(ident) != null) {
        String msg = "\"" + ident + "\" is already defined in this scope";
        throw new SemanticErrorException(ctx.getStart(), msg);
      } else {
        st.add(ident, func);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitFunc(FuncContext ctx) {
    st = new SymbolTable(st);
    if (ctx.paramList() != null) {
      visit(ctx.paramList());
    }
    visit(ctx.stat());
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitParamList(ParamListContext ctx) {
    for (ParamContext param : ctx.param()) {
      String ident = param.ident().getText();
      if (st.lookup(ident) != null) {
        String msg = "\"" + ident + "\" is already defined in this scope";
        throw new SemanticErrorException(ctx.getParent().getStart(), msg);
      } else {
        st.add(ident, param.type());
      }
    }
    return null;
  }

  @Override
  public Void visitVarDeclStat(VarDeclStatContext ctx) {
    String ident = ctx.ident().getText();
    ParserRuleContext c = st.lookup(ident);
    if (c != null && !(c instanceof FuncContext)) {
      String msg = "\"" + ident + "\" is already defined in this scope";
      throw new SemanticErrorException(ctx.getStart(), msg);
    } else {
      st.add(ident, ctx.type());
    }
    Type identType = Utils.getType(ctx.type());
    Type assignRhsType = Utils.getType(ctx.assignRhs(), st);
    if (!identType.equals(assignRhsType)) {
      String assignRhs = ctx.assignRhs().getText();
      String msg = "Incompatible type at \"" + assignRhs + "\" (expected: "
          + identType.toString() + ", actual: " + assignRhsType + ")";
      throw new SemanticErrorException(ctx.assignRhs().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitAssignStat(AssignStatContext ctx) {
    Type assignLhsType = Utils.getType(ctx.assignLhs(), st);
    Type assignRhsType = Utils.getType(ctx.assignRhs(), st);
    if (!assignLhsType.equals(assignRhsType)) {
      String assignRhs = ctx.assignRhs().getText();
      String msg = "Incompatible type at \"" + assignRhs + "\" (expected: "
          + assignLhsType.toString() + ", actual: " + assignRhsType.toString()
          + ")";
      throw new SemanticErrorException(ctx.assignRhs().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitFreeStat(FreeStatContext ctx) {
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!(exprType instanceof PairType || exprType instanceof ArrayType)) {
      String msg = "Incompatible type " + exprType.toString();
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitExitStat(ExitStatContext ctx) {
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!Utils.isSameBaseType(exprType, BaseLiter.INT)) {
      String expr = ctx.expr().getText();
      String msg = "Incompatible type at \"" + expr + "\" (expected: "
          + BaseLiter.INT + ", actual: " + exprType.toString() + ")";
      throw new SemanticErrorException(ctx.expr().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitWhileStat(WhileStatContext ctx) {
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!Utils.isSameBaseType(exprType, BaseLiter.BOOL)) {
      String expr = ctx.expr().getText();
      String msg = "Incompatible type at \"" + expr + "\" (expected: "
          + BaseLiter.BOOL + ", actual: " + exprType.toString() + ")";
      throw new SemanticErrorException(ctx.expr().getStart(), msg);
    }
    visit(ctx.expr());
    st = new SymbolTable(st);
    visit(ctx.stat());
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitScopingStat(ScopingStatContext ctx) {
    st = new SymbolTable(st);
    visit(ctx.stat());
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitIdent(IdentContext ctx) {
    String ident = ctx.getText();
    if (st.lookupAll(ident) == null) {
      String msg = "Variable \"" + ident + "\" is not defined in this scope";
      throw new SemanticErrorException(ctx.getParent().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitReadStat(ReadStatContext ctx) {

    Type targetType = Utils.getType(ctx.assignLhs(), st);
    if (!(Utils.isSameBaseType(targetType, BaseLiter.INT) | Utils
        .isSameBaseType(targetType, BaseLiter.CHAR))) {
      String msg = "Expected type INT or CHAR. Actual type " + targetType;
      throw new SemanticErrorException(ctx.start, msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitReturnStat(ReturnStatContext ctx) {
    // Check if in global scope
    ParserRuleContext context = ctx.getParent();
    while (!(context instanceof FuncContext)) {
      if (context instanceof ProgramContext) {
        String msg = "Cannot return from the global scope. ";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
      context = context.getParent();
    }
    // Visit expr first to check it for semantic errors
    visit(ctx.expr());
    // Check return exp type matches func type
    if (!(Utils.getType(ctx.expr(), st).equals(Utils
        .getType(((FuncContext) context).type())))) {
      String msg = "Incompatible type at " + ctx.expr().getText()
          + " (expected: INT, actual: "
          + Utils.getType(ctx.expr(), st).toString() + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitIfThenElseStat(IfThenElseStatContext ctx) {
    visit(ctx.expr());
    Type ifExpr = Utils.getType(ctx.expr(), st);
    if (Utils.isSameBaseType(ifExpr, BaseLiter.BOOL)) {
      st = new SymbolTable(st);
      visit(ctx.stat(0));
      st = st.getEncSymTable();
      st = new SymbolTable(st);
      visit(ctx.stat(1));
      st = st.getEncSymTable();
    }
    return null;
  }

  @Override
  public Void visitRhsCall(RhsCallContext ctx) {
    String ident = ctx.ident().getText();
    FuncContext func = (FuncContext) st.lookupAll(ident);
    int paramSize = func.paramList().param().size();
    int argSize = ctx.argList().expr().size();
    if (paramSize != argSize) {
      String msg = "Incorrect number of parameters for \"" + ident
          + "\" (expected: " + paramSize + ", actual: " + argSize + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }

    for (int i = 0; i < paramSize; i++) {
      Type paramType = Utils.getType(func.paramList().param(i).type());
      Type argType = Utils.getType(ctx.argList().expr(i), st);
      if (!paramType.equals(argType)) {
        String msg = "Incompatible type at " + ctx.argList().expr(i).getText()
            + " (expected: " + paramType + ", actual: " + argType + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitUnaryOper(BasicParser.UnaryOperContext ctx) {
    if (ctx.MINUS() != null) {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof IntExprContext)) {
        String msg = "Incompatible type at "
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + " (expected: INT, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString() == "!") {
      Type type = Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st);
      if (type.toString() != "BOOL") {
        String msg = "Incompatible type at "
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + " (expected: BOOL, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString().equals("len")) {
      Type type = Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st);
      if (!(type instanceof ArrayType)) {
        String msg = "Incompatible type at "
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + " (expected: ARRAY, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString().equals("ord")) {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof CharExprContext)) {
        String msg = "Incompatible type at "
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + " (expected: CHAR, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString().equals("chr")) {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof IntExprContext)) {
        String msg = "Incompatible type at "
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + " (expected: INT, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinaryOper(BasicParser.BinaryOperContext ctx) {
    String binOp = "*/%+";
    Type type1 = Utils
        .getType(((BinOpExprContext) ctx.getParent()).expr(0), st);
    Type type2 = Utils
        .getType(((BinOpExprContext) ctx.getParent()).expr(1), st);
    if (ctx.MINUS() != null || (binOp.contains(ctx.BINARY_OPER().toString()))) {
      if (type2.toString() != "INT") {
        String msg = "Incompatible type at "
            + ((BinOpExprContext) ctx.getParent()).expr(1).getText()
            + " (expected: INT, actual: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(1), st)
                .toString() + ")";
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(1).getStart(), msg);
      } else if (type1.toString() != "INT") {
        String msg = "Incompatible type at "
            + ((BinOpExprContext) ctx.getParent()).expr(0).getText()
            + " (expected: INT, actual: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(0), st)
                .toString() + ")";
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(0).getStart(), msg);
      }
    } else if (ctx.BINARY_OPER().toString().equals(">=")
        || ctx.BINARY_OPER().toString().equals(">")
        || ctx.BINARY_OPER().toString().equals("<=")
        || ctx.BINARY_OPER().toString().equals("<")) {
      if (type2.toString() != "INT" && type2.toString() != "CHAR") {
        String msg = "Incompatible type "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(1), st)
                .toString();
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(1).getStart(), msg);
      } else if (type1.toString() != type2.toString()) {
        String msg = "Incompatible type at "
            + ((BinOpExprContext) ctx.getParent()).expr(0).getText()
            + " (expected: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(1), st)
                .toString()
            + ", actual: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(0), st)
                .toString() + ")";
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(0).getStart(), msg);
      }
    } else if (ctx.BINARY_OPER().toString().equals("==")
        || ctx.BINARY_OPER().toString().equals("!=")) {
      if (type2.toString() != type1.toString()) {
        String msg = "Incompatible type at "
            + ((BinOpExprContext) ctx.getParent()).expr(0).getText()
            + " (expected: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(1), st)
                .toString()
            + ", actual: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(0), st)
                .toString() + ")";
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(0).getStart(), msg);
      }
    } else if (ctx.BINARY_OPER().toString().equals("&&")
        || ctx.BINARY_OPER().toString().equals("||")) {
      if (type2.toString() != "BOOL") {
        String msg = "Incompatible type at "
            + ((BinOpExprContext) ctx.getParent()).expr(1).getText()
            + " (expected: BOOL, actual: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(1), st)
                .toString() + ")";
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(1).getStart(), msg);
      } else if (type1.toString() != "BOOL") {
        String msg = "Incompatible type at "
            + ((BinOpExprContext) ctx.getParent()).expr(0).getText()
            + " (expected: BOOL, actual: "
            + Utils.getType(((BinOpExprContext) ctx.getParent()).expr(0), st)
                .toString() + ")";
        throw new SemanticErrorException(((BinOpExprContext) ctx.getParent())
            .expr(0).getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitUnOpExpr(UnOpExprContext ctx) {
    visitUnaryOper(ctx.unaryOper());
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpExpr(BinOpExprContext ctx) {
    visitBinaryOper(ctx.binaryOper());
    return visitChildren(ctx);
  }

}
