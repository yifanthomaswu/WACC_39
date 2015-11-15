package wacc.visitor;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.*;
import antlr.BasicParser.LhsArrayElemContext;
import antlr.BasicParser.*;
import wacc.symboltable.SymbolTable;
import wacc.visitor.utils.*;

public class SemanticVisitor extends BasicParserBaseVisitor<Void> {

  private SymbolTable st;

  @Override
  public Void visitProgram(BasicParser.ProgramContext ctx) {
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
  public Void visitFunc(BasicParser.FuncContext ctx) {
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
    return visitChildren(ctx);
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

  @Override
  public Void visitScopingStat(BasicParser.ScopingStatContext ctx) {
    st = new SymbolTable(st);
    visit(ctx.stat());
    st = st.getEncSymTable();
    return null;
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
  public Void visitReadStat(BasicParser.ReadStatContext ctx) {
    if (ctx.assignLhs() instanceof LhsIdentContext) {
      String ident = ctx.assignLhs().getText();
      TypeContext identType = ((VarDeclStatContext) st.lookup(ident)).type();
      if (!(identType.getText().equals("int") |
            identType.getText().equals("char"))) {
        String msg = "Incompatible type " + identType.getText();
        throw new SemanticErrorException(ctx.start, msg);
      }

    }
    if (ctx.assignLhs() instanceof LhsArrayElemContext) {
    }

    if (ctx.assignLhs() instanceof LhsPairElemContext) {
    }
    return visitChildren(ctx);
  }

  //
  // @Override
  // public Void visitFreeStat(BasicParser.FreeStatContext ctx) {
  // return visitChildren(ctx);
  // }

  @Override
  public Void visitReturnStat(BasicParser.ReturnStatContext ctx) {
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

  @Override
  public Void visitExitStat(BasicParser.ExitStatContext ctx) {
    // Visit expr first to check it for semantic errors
    visit(ctx.expr());
    // Carry out check that return value of expr is an INT as required by EXIT
    // Stat
    if (!(ctx.expr() instanceof IntExprContext)) {
      String msg = "Incompatible type at " + ctx.expr().getText()
          + " (expected: INT, actual: " + Utils.getType(ctx.expr(), st).toString()
          + ")";
      throw new SemanticErrorException(ctx.expr().getStart(), msg);
    }
    return visitChildren(ctx);
  }

  // @Override
  // public Void visitPrintStat(BasicParser.PrintStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPrintlnStat(BasicParser.PrintlnStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIfThenElseStat(BasicParser.IfThenElseStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitWhileStat(BasicParser.WhileStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitBeginStat(BasicParser.BeginStatContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitStatList(BasicParser.StatListContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitLhsIdent(BasicParser.LhsIdentContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitLhsArrayElem(BasicParser.LhsArrayElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitLhsPairElem(BasicParser.LhsPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsExpr(BasicParser.RhsExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsArrayLiter(BasicParser.RhsArrayLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsNewPair(BasicParser.RhsNewPairContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsPairElem(BasicParser.RhsPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitRhsFunctionCall(BasicParser.RhsFunctionCallContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArgList(BasicParser.ArgListContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitFstPairElem(BasicParser.FstPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitSndPairElem(BasicParser.SndPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  @Override
  public Void visitType(BasicParser.TypeContext ctx) {
    return visitChildren(ctx);
  }
  //
  // @Override
  // public Void visitBaseType(BasicParser.BaseTypeContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayType(BasicParser.ArrayTypeContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairType(BasicParser.PairTypeContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairElemBase(BasicParser.PairElemBaseContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairElemArray(BasicParser.PairElemArrayContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairPairElem(BasicParser.PairPairElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIntExpr(BasicParser.IntExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitBoolExpr(BasicParser.BoolExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitCharExpr(BasicParser.CharExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitStringExpr(BasicParser.StringExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairExpr(BasicParser.PairExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIdentExpr(BasicParser.IdentExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayElemExpr(BasicParser.ArrayElemExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitUnOpExpr(BasicParser.UnOpExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitBinOpExpr(BasicParser.BinOpExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitParensExpr(BasicParser.ParensExprContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitUnaryOper(BasicParser.UnaryOperContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitBinaryOper(BasicParser.BinaryOperContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitIdent(BasicParser.IdentContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayElem(BasicParser.ArrayElemContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  
  
  
//   @Override
//   public Void visitIntLiter(BasicParser.IntLiterContext ctx) {
//   return visitChildren(ctx);
//   }
  //
  // @Override
  // public Void visitBoolLiter(BasicParser.BoolLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  
  
  
  
  //
  // @Override
  // public Void visitCharLiter(BasicParser.CharLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitStringLiter(BasicParser.StringLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitArrayLiter(BasicParser.ArrayLiterContext ctx) {
  // return visitChildren(ctx);
  // }
  //
  // @Override
  // public Void visitPairLiter(BasicParser.PairLiterContext ctx) {
  // return visitChildren(ctx);
  // }

}
