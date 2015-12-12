package wacc.visitor.semantic_error;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.*;
import antlr.WACCParser.*;
import wacc.visitor.SymbolTable;
import wacc.visitor.type.*;

public class SemanticVisitor extends WACCParserBaseVisitor<Void> {

  private SymbolTable st;

  @Override
  public Void visitProgram(ProgramContext ctx) {
    st = new SymbolTable(null);
    for (FuncContext func : ctx.func()) {
      String ident = func.ident().getText();
      if (!Utils.isDefinable(func, st)) {
        String signature = ident + "(";
        if (Utils.getParamSize(func) != 0) {
          for (ParamContext c : func.paramList().param()) {
            signature += Utils.getType(c.type()).toString() + ",";
          }
          signature = signature.substring(0, signature.length() - 1);
        }
        signature += ")";
        String msg = "\"" + signature + "\" is already defined in this scope";
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
      if (st.lookupT(ident) != null) {
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
    visit(ctx.assignRhs());
    String ident = ctx.ident().getText();
    if (st.lookupT(ident) != null) {
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
          + identType + ", actual: " + assignRhsType + ")";
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
          + assignLhsType + ", actual: " + assignRhsType + ")";
      throw new SemanticErrorException(ctx.assignRhs().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitReadStat(ReadStatContext ctx) {
    Type exprType = Utils.getType(ctx.assignLhs(), st);
    if (!(Utils.isSameBaseType(exprType, BaseLiter.INT) || Utils
        .isSameBaseType(exprType, BaseLiter.CHAR))) {
      String msg = "Incompatible type at \"" + ctx.assignLhs().getText()
          + "\" (expected: INT or CHAR, actual: " + exprType + ")";
      throw new SemanticErrorException(ctx.start, msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitFreeStat(FreeStatContext ctx) {
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!(exprType instanceof PairType || exprType instanceof ArrayType)) {
      String msg = "Incompatible type " + exprType;
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
          + BaseLiter.INT + ", actual: " + exprType + ")";
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
          + BaseLiter.BOOL + ", actual: " + exprType + ")";
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
          + BaseLiter.BOOL + ", actual: " + exprType + ")";
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
    if (st.lookupAllT(ident) == null && st.lookupAllF(ident).size() == 0) {
      String msg = "\"" + ident + "\" is not defined in this scope";
      throw new SemanticErrorException(ctx.getParent().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitRhsCall(RhsCallContext ctx) {
    if (!Utils.isCallable(ctx, st)) {
      String signature = ctx.ident().getText() + "(";
      if (Utils.getArgSize(ctx) != 0) {
        for (ExprContext c : ctx.argList().expr()) {
          signature += Utils.getType(c, st).toString() + ",";
        }
        signature = signature.substring(0, signature.length() - 1);
      }
      signature += ")";
      String msg = "\"" + signature + "\" is not defined in this scope";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitArrayElem(ArrayElemContext ctx) {
    for (ExprContext c : ctx.expr()) {
      Type exprType = Utils.getType(c, st);
      if (!Utils.isSameBaseType(exprType, BaseLiter.INT)) {
        String msg = "Incompatible type at \"" + c.getText()
            + "\" (expected: INT, actual: " + exprType + ")";
        throw new SemanticErrorException(c.getStart(), msg);
      }
    }

    Type identType = Utils.getType(ctx.ident(), st);
    if (!(identType instanceof ArrayType)
        || ((ArrayType) identType).getLevel() < ctx.expr().size()) {
      String expected = "T";
      for (int i = 0; i < ctx.expr().size(); i++) {
        expected += "[]";
      }
      String msg = "Incompatible type at " + ctx.getText() + " (expected: "
          + expected + ", actual: " + identType + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
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
      case "chr":
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
  public Void visitBinOpPrec1Expr(BinOpPrec1ExprContext ctx) {
    Type[] exprs = new Type[2];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = Utils.getType(ctx.expr(i), st);
      if (!Utils.isSameBaseType(exprs[i], BaseLiter.INT)) {
        String msg = "Incompatible type at \"" + ctx.expr(i).getText()
            + "\" (expected: INT, actual: " + exprs[i] + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpPrec2Expr(BinOpPrec2ExprContext ctx) {
    Type[] exprs = new Type[2];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = Utils.getType(ctx.expr(i), st);
      if (!Utils.isSameBaseType(exprs[i], BaseLiter.INT)) {
        String msg = "Incompatible type at \"" + ctx.expr(i).getText()
            + "\" (expected: INT, actual: " + exprs[i] + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpPrec3Expr(BinOpPrec3ExprContext ctx) {
    Type[] exprs = new Type[2];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = Utils.getType(ctx.expr(i), st);
      if (!(Utils.isSameBaseType(exprs[i], BaseLiter.INT) || Utils
          .isSameBaseType(exprs[i], BaseLiter.CHAR))) {
        String msg = "Incompatible type at \"" + ctx.expr(i).getText()
            + "\" (expected: INT or CHAR, actual: " + exprs[i] + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }

    if (!exprs[0].equals(exprs[1])) {
      String msg = "Incompatible type at \"" + ctx.expr(1).getText()
          + "\" (expected: " + exprs[0] + ", actual: " + exprs[1] + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpPrec4Expr(BinOpPrec4ExprContext ctx) {
    Type[] exprs = new Type[2];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = Utils.getType(ctx.expr(i), st);
    }

    if (!exprs[0].equals(exprs[1])) {
      String msg = "Incompatible type at \"" + ctx.expr(1).getText()
          + "\" (expected: " + exprs[0] + ", actual: " + exprs[1] + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpPrec5Expr(BinOpPrec5ExprContext ctx) {
    Type[] exprs = new Type[2];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = Utils.getType(ctx.expr(i), st);
      if (!Utils.isSameBaseType(exprs[i], BaseLiter.BOOL)) {
        String msg = "Incompatible type at \"" + ctx.expr(i).getText()
            + "\" (expected: BOOL, actual: " + exprs[i] + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitBinOpPrec6Expr(BinOpPrec6ExprContext ctx) {
    Type[] exprs = new Type[2];
    for (int i = 0; i < exprs.length; i++) {
      exprs[i] = Utils.getType(ctx.expr(i), st);
      if (!Utils.isSameBaseType(exprs[i], BaseLiter.BOOL)) {
        String msg = "Incompatible type at \"" + ctx.expr(i).getText()
            + "\" (expected: BOOL, actual: " + exprs[i] + ")";
        throw new SemanticErrorException(ctx.getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

}
