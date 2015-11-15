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
        st.add(ident, func.type());
      }
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitFunc(FuncContext ctx) {
    st = new SymbolTable(st);
    if (ctx.paramList() != null)
      visit(ctx.paramList());
    visit(ctx.funcStat()); // TODO
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitParamList(BasicParser.ParamListContext ctx) {
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
  public Void visitVarDeclStat(BasicParser.VarDeclStatContext ctx) {
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
  public Void visitAssignStat(BasicParser.AssignStatContext ctx) {
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
  public Void visitFreeStat(BasicParser.FreeStatContext ctx) {
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!(exprType instanceof PairType || exprType instanceof ArrayType)) {
      String msg = "Incompatible type " + exprType.toString();
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);
  }

  @Override
  public Void visitExitStat(BasicParser.ExitStatContext ctx) {
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
  public Void visitWhileStat(BasicParser.WhileStatContext ctx) {
    visit(ctx.expr());
    Type exprType = Utils.getType(ctx.expr(), st);
    if (!Utils.isSameBaseType(exprType, BaseLiter.BOOL)) {
      String expr = ctx.expr().getText();
      String msg = "Incompatible type at \"" + expr + "\" (expected: "
          + BaseLiter.BOOL + ", actual: " + exprType.toString() + ")";
      throw new SemanticErrorException(ctx.expr().getStart(), msg);
    }
    st = new SymbolTable(st);
    visit(ctx.stat());
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitScopingStat(BasicParser.ScopingStatContext ctx) {
    st = new SymbolTable(st);
    visit(ctx.stat());
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitIdent(BasicParser.IdentContext ctx) {
    String ident = ctx.getText();
    if (st.lookupAll(ident) == null) {
      String msg = "Variable \"" + ident + "\" is not defined in this scope";
      throw new SemanticErrorException(ctx.getParent().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  // @Override
  // public Void visitFuncStat(BasicParser.FuncStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  //
  // @Override
  // public Void visitParamList(BasicParser.ParamListContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitParam(BasicParser.ParamContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitSkipStat(BasicParser.SkipStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitAssignVarStat(BasicParser.AssignVarStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitAssignLhsToRhsStat(BasicParser.AssignLhsToRhsStatContext
  // ctx) {
  // return visitChildren(ctx);
  // }
  //

  @Override
  public Void visitReadStat(ReadStatContext ctx) {

    Type targetType = Utils.getType(ctx.assignLhs(), st);
    if (!(Utils.isSameBaseType(targetType, BaseLiter.INT)
        | Utils.isSameBaseType(targetType, BaseLiter.CHAR))) {
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
    context = ((FuncContext) context).type();
    if (context.equals(ctx.expr())) {
      String msg = "Incompatible type at " + ctx.expr().getText()
          + " (expected: INT, actual: " + ctx.expr().getClass().getSimpleName()
          + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return visitChildren(ctx);

  }

  // @Override
  // public Void visitPrintStat(PrintStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPrintlnStat(PrintlnStatContext ctx) {
  // return visitChildren(ctx);
  // }

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
     return visitChildren(ctx);
   }

  //
  // @Override
  // public Void visitBeginStat(BeginStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitStatList(StatListContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitLhsIdent(LhsIdentContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitLhsArrayElem(LhsArrayElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitLhsPairElem(LhsPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsExpr(RhsExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsArrayLiter(RhsArrayLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsNewPair(RhsNewPairContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsPairElem(RhsPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsFunctionCall(RhsFunctionCallContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArgList(ArgListContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitFstPairElem(FstPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitSndPairElem(SndPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  @Override
  public Void visitType(TypeContext ctx) {
    return visitChildren(ctx);
  }

  //
  // @Override
  // public Void visitBaseType(BaseTypeContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayType(ArrayTypeContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairType(PairTypeContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairElemBase(PairElemBaseContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairElemArray(PairElemArrayContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairPairElem(PairPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIntExpr(IntExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitBoolExpr(BoolExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitCharExpr(CharExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitStringExpr(StringExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairExpr(PairExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIdentExpr(IdentExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayElemExpr(ArrayElemExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  @Override
  public Void visitUnOpExpr(BasicParser.UnOpExprContext ctx) {
    visitUnaryOper(ctx.unaryOper());
    return visitChildren(ctx);
  }

  //
  // @Override
  // public Void visitBinOpExpr(BinOpExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  @Override
  public Void visitParensExpr(BasicParser.ParensExprContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public Void visitUnaryOper(BasicParser.UnaryOperContext ctx) {
    if (ctx.MINUS() != null) {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof IntExprContext)) {
        String msg = "Incompatible type at"
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + "(expected: INT, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString() == "!") {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof BoolExprContext)) {
        String msg = "Incompatible type at"
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + "(expected: BOOL, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString() == "len") {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof ArrayElemExprContext)) {
        String msg = "Incompatible type at"
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + "(expected: ARRAY, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString() == "ord") {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof CharExprContext)) {
        String msg = "Incompatible type at"
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + "(expected: CHAR, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    } else if (ctx.UNARY_OPER().toString() == "chr") {
      if (!(((UnOpExprContext) ctx.getParent()).expr() instanceof IntExprContext)) {
        String msg = "Incompatible type at"
            + ((UnOpExprContext) ctx.getParent()).expr().getText()
            + "(expected: INT, actual: "
            + Utils.getType(((UnOpExprContext) ctx.getParent()).expr(), st)
                .toString() + ")";
        throw new SemanticErrorException(((UnOpExprContext) ctx.getParent())
            .expr().getStart(), msg);
      }
    }
    return visitChildren(ctx);
  }

  //
  // @Override
  // public Void visitBinaryOper(BinaryOperContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIdent(IdentContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayElem(ArrayElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIntLiter(IntLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitBoolLiter(BoolLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitCharLiter(CharLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitStringLiter(StringLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayLiter(ArrayLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairLiter(PairLiterContext ctx) {
  // return visitChildren(ctx);
  // }

}
