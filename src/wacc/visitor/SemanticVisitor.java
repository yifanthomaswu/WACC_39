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
    ParserRuleContext context = st.lookup(ident);
    if (context != null && !(context instanceof FuncContext)) {
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
  public Void visitReadStat(ReadStatContext ctx) {
    Type exprType = Utils.getType(ctx.assignLhs(), st);
    if (!(Utils.isSameBaseType(exprType, BaseLiter.INT) || Utils
        .isSameBaseType(exprType, BaseLiter.CHAR))) {
      String msg = "Expected type INT or CHAR. Actual type " + exprType;
      throw new SemanticErrorException(ctx.start, msg);
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
  public Void visitReturnStat(ReturnStatContext ctx) {
    // Check if in global scope
    ParserRuleContext context = ctx.getParent();
    while (!(context instanceof FuncContext)) {
      if (context instanceof ProgramContext) {
        String msg = "Cannot return from the global scope.";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
      context = context.getParent();
    }
    // Check return exp type matches func type
    Type exprType = Utils.getType(ctx.expr(), st);
    Type funcType = Utils.getType(((FuncContext) context).type());
    if (!exprType.equals(funcType)) {
      String msg = "Incompatible type at " + ctx.expr().getText()
          + " (expected: " + funcType + ", actual: " + exprType + ")";
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
  public Void visitIfStat(IfStatContext ctx) {
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!Utils.isSameBaseType(exprType, BaseLiter.BOOL)) {
      String expr = ctx.expr().getText();
      String msg = "Incompatible type at \"" + expr + "\" (expected: "
          + BaseLiter.BOOL + ", actual: " + exprType.toString() + ")";
      throw new SemanticErrorException(ctx.expr().getStart(), msg);
    }

    visit(ctx.expr());
    st = new SymbolTable(st);
    visit(ctx.stat(0));
    st = st.getEncSymTable();

    st = new SymbolTable(st);
    visit(ctx.stat(1));
    st = st.getEncSymTable();
    return null;
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
  public Void visitRhsCall(RhsCallContext ctx) {
    String ident = ctx.ident().getText();
    FuncContext func = (FuncContext) st.lookupAll(ident);
    int paramSize = 0;
    if (func.paramList() != null) {
      paramSize = func.paramList().param().size();
    }
    int argSize = 0;
    if (ctx.argList() != null) {
      argSize = ctx.argList().expr().size();
    }
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
  public Void visitUnOpExpr(UnOpExprContext ctx) {
    String oper;
    if (ctx.unaryOper().MINUS() == null) {
      oper = ctx.unaryOper().UNARY_OPER().getText();
    } else {
      oper = ctx.unaryOper().MINUS().getText();
    }
    Type exprType = Utils.getType(ctx.expr(), st);
    BaseLiter expectedType = null;
    switch (oper) {
      case "!":
        expectedType = BaseLiter.BOOL;
        break;
      case "-":
      case "!chr":
        expectedType = BaseLiter.INT;
        break;
      case "ord":
        expectedType = BaseLiter.CHAR;
        break;
    }
    String expr = ctx.expr().getText();
    if (expectedType == null) {
      if (!(exprType instanceof ArrayType)) {
        String msg = "Incompatible type at \"" + expr
            + "\" (expected: T[], actual: " + exprType + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    } else {
      if (!Utils.isSameBaseType(exprType, expectedType)) {
        String msg = "Incompatible type at \"" + expr + "\" (expected: "
            + expectedType + ", actual: " + exprType + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpExpr(BinOpExprContext ctx) {
    String oper;
    if (ctx.binaryOper().MINUS() == null) {
      oper = ctx.binaryOper().BINARY_OPER().getText();
    } else {
      oper = ctx.binaryOper().MINUS().getText();
    }
    Type[] exprTypes = new Type[2];
    for (int i = 0; i < exprTypes.length; i++) {
      exprTypes[i] = Utils.getType(ctx.expr(i), st);
    }
    BaseLiter expectedType = null;
    BaseLiter altExpectedType = null;
    switch (oper) {
      case "*":
      case "/":
      case "%":
      case "+":
      case "-":
        expectedType = BaseLiter.INT;
        break;
      case ">":
      case ">=":
      case "<":
      case "<=":
        expectedType = BaseLiter.INT;
        altExpectedType = BaseLiter.CHAR;
        break;
      case "&&":
      case "||":
        expectedType = BaseLiter.BOOL;
        break;
    }
    String expr = ctx.getText();
    if (expectedType == null && altExpectedType == null) {
      if (!exprTypes[0].equals(exprTypes[1])) {
        String msg = "Incompatible type at \"" + expr + "\" (expected: "
            + exprTypes[0] + ", actual: " + exprTypes[1] + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    } else if (altExpectedType == null) {
      for (int i = 0; i < exprTypes.length; i++) {
        if (!Utils.isSameBaseType(exprTypes[i], expectedType)) {
          String msg = "Incompatible type at \"" + expr + "\" (expected: "
              + expectedType + ", actual: " + exprTypes[i] + ")";
          throw new SemanticErrorException(ctx.getStart(), msg);
        }
      }
    } else {
      for (int i = 0; i < exprTypes.length; i++) {
        if (!Utils.isSameBaseType(exprTypes[i], expectedType)
            && !Utils.isSameBaseType(exprTypes[i], altExpectedType)) {
          String msg = "Incompatible type at \"" + expr + "\" (expected: "
              + expectedType + " or " + altExpectedType + ", actual: "
              + exprTypes[i] + ")";
          throw new SemanticErrorException(ctx.getStart(), msg);
        }
      }
    }
    return visitChildren(ctx);
  }

}
