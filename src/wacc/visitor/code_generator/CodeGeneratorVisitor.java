package wacc.visitor.code_generator;

import antlr.*;
import antlr.BasicParser.*;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private int currentStackPointer = 0;
  private SymbolTable st;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
  }

  @Override
  public Void visitProgram(ProgramContext ctx) {
    st = new SymbolTable(null);
    int size = sizeOfDecl(ctx.stat());
    for (FuncContext c : ctx.func()) {
      visit(c);
    }
    writer.addLabel("main");
    writer.addInst(Inst.PUSH, "{lr}");
    if (size > 0) {
      writer.addInst(Inst.SUB, "sp, sp, #" + size);
    }
    visit(ctx.stat());
    if (size > 0) {
      writer.addInst(Inst.ADD, "sp, sp, #" + size);
    }
    writer.addInst(Inst.LDR, "r0, =0");
    writer.addInst(Inst.POP, "{pc}");
    ;
    writer.addLtorg();
    return null;
  }

  @Override
  public Void visitFunc(FuncContext ctx) {
    writer.addLabel("f_" + ctx.ident().getText());
    writer.addInst(Inst.PUSH, "{lr}");
    visitChildren(ctx);
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();
    return null;
  }

  @Override
  public Void visitReturnStat(ReturnStatContext ctx) {
    visit(ctx.expr());
    writer.addInst(Inst.MOV, "r0, r4");
    writer.addInst(Inst.POP, "{pc}");
    return null;
  }

  @Override
  public Void visitExitStat(ExitStatContext ctx) {
    visit(ctx.expr());
    writer.addInst(Inst.MOV, "r0, r4");
    writer.addInst(Inst.BL, "exit");
    return null;
  }

  public Void visitIfStat(IfStatContext ctx) {
    visit(ctx.expr());
    writer.addInst(Inst.CMP, "r4, #0");
    String[] pair = writer.getLabelLPair();
    writer.addInst(Inst.BEQ, pair[0]);

    st = new SymbolTable(st);
    visit(ctx.stat(0));
    st = st.getEncSymTable();

    writer.addInst(Inst.B, pair[1]);
    writer.addLabel(pair[0]);

    st = new SymbolTable(st);
    visit(ctx.stat(1));
    st = st.getEncSymTable();

    writer.addLabel(pair[1]);
    return null;
  }

  @Override
  public Void visitWhileStat(WhileStatContext ctx) {
    String[] pair = writer.getLabelLPair();
    writer.addInst(Inst.B, pair[0]);
    writer.addLabel(pair[1]);

    st = new SymbolTable(st);
    visit(ctx.stat());
    st = st.getEncSymTable();

    writer.addLabel(pair[0]);
    visit(ctx.expr());
    writer.addInst(Inst.CMP, "r4, #1");
    writer.addInst(Inst.BEQ, pair[1]);
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
  public Void visitIntExpr(IntExprContext ctx) {
    writer.addInst(Inst.LDR, "r4, =" + ctx.getText());
    return null;
  }

  @Override
  public Void visitBoolLiter(BoolLiterContext ctx) {
    if (ctx.getText().equals("true")) {
      writer.addInst(Inst.MOV, "r4, #1");
    } else {
      writer.addInst(Inst.MOV, "r4, #0");
    }
    return null;
  }

  @Override
  public Void visitCharLiter(CharLiterContext ctx) {
    String c = ctx.getText();
    switch (c.charAt(2)) {
      case '0':
        c = "0";
        break;
      case 'b':
        c = "8";
        break;
      case 't':
        c = "9";
        break;
      case 'n':
        c = "10";
        break;
      case 'f':
        c = "12";
        break;
      case 'r':
        c = "13";
        break;
      case '\"':
        c = "\'\"\'";
        break;
      case '\'':
        c = "\'\'\'";
        break;
      case '\\':
        c = "\'\\\'";
        break;
    }
    writer.addInst(Inst.MOV, "r4, #" + c);
    return null;
  }

  @Override
  public Void visitStringLiter(StringLiterContext ctx) {
    String s = ctx.getText();
    String msg = writer.addMsg(s.substring(1, s.length() - 1));
    writer.addInst(Inst.LDR, "r4, =" + msg);
    return null;
  }

  @Override
  public Void visitBinOpPrec1Expr(BinOpPrec1ExprContext ctx) {
    visitChildren(ctx);
    if (ctx.MULT() != null) {
      writer.addInst(Inst.SMULL, "r4, r5, r4, r5");
      writer.addInst(Inst.CMP, "r5, r4, ASR #31");
      writer.addInst(Inst.BLNE, writer.p_throw_overflow_error());
    } else {
      writer.addInst(Inst.MOV, "r0, r4");
      writer.addInst(Inst.MOV, "r1, r5");
      writer.addInst(Inst.BL, writer.p_check_divide_by_zero());
      if (ctx.DIV() != null) {
        writer.addInst(Inst.BL, "__aeabi_idiv");
      } else {
        writer.addInst(Inst.BL, "__aeabi_idivmod");
      }
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec2Expr(BinOpPrec2ExprContext ctx) {
    visitChildren(ctx);
    if (ctx.PLUS() != null) {
      writer.addInst(Inst.ADDS, "r4, r4, r5");
    } else {
      writer.addInst(Inst.SUBS, "r4, r4, r5");
    }
    writer.addInst(Inst.BLVS, writer.p_throw_overflow_error());
    return null;
  }

  @Override
  public Void visitBinOpPrec3Expr(BinOpPrec3ExprContext ctx) {
    visitChildren(ctx);
    writer.addInst(Inst.CMP, "r4, r5");
    if (ctx.GRT() != null) {
      writer.addInst(Inst.MOVGT, "r4, #1");
      writer.addInst(Inst.MOVLE, "r4, #0");
    } else if (ctx.GRT_EQUAL() != null) {
      writer.addInst(Inst.MOVGE, "r4, #1");
      writer.addInst(Inst.MOVLT, "r4, #0");
    } else if (ctx.LESS() != null) {
      writer.addInst(Inst.MOVLT, "r4, #1");
      writer.addInst(Inst.MOVGE, "r4, #0");
    } else {
      writer.addInst(Inst.MOVLE, "r4, #1");
      writer.addInst(Inst.MOVGT, "r4, #0");
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec4Expr(BinOpPrec4ExprContext ctx) {
    visitChildren(ctx);
    writer.addInst(Inst.CMP, "r4, r5");
    if (ctx.EQUAL() != null) {
      writer.addInst(Inst.MOVEQ, "r4, #1");
      writer.addInst(Inst.MOVNE, "r4, #0");
    } else {
      writer.addInst(Inst.MOVNE, "r4, #1");
      writer.addInst(Inst.MOVEQ, "r4, #0");
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec5Expr(BinOpPrec5ExprContext ctx) {
    visitChildren(ctx);
    writer.addInst(Inst.AND, "r4, r4, r5");
    return null;
  }

  @Override
  public Void visitBinOpPrec6Expr(BinOpPrec6ExprContext ctx) {
    visitChildren(ctx);
    writer.addInst(Inst.ORR, "r4, r4, r5");
    return null;
  }

  // @Override
  // public Void visitCompStat(CompStatContext ctx) {
  // int count = 0;
  // while(ctx.getChild(count) instanceof VarDeclStatContext) {
  // count++;
  // }
  // visit
  // }

  private int sizeOfDecl(StatContext ctx) {
    if (ctx instanceof VarDeclStatContext) {
      int size = 0;
      if (((VarDeclStatContext) ctx).type().baseType() != null) {
        switch (((VarDeclStatContext) ctx).type().getText()) {
          case "bool":
          case "char":
            size = 1;
            currentStackPointer++;
            break;
          case "int":
          case "string":
            size = 4;
            currentStackPointer += 4;
            break;
        }
      } else if (((VarDeclStatContext) ctx).type().arrayType() != null) {
        size = 4;
        currentStackPointer += 4;
      }
      st.add(((VarDeclStatContext) ctx).ident().getText(), currentStackPointer);
      return size;
    } else if (ctx instanceof CompStatContext) {
      int result = 0;
      for (StatContext c : ((CompStatContext) ctx).stat()) {
        result += sizeOfDecl(c);
      }
      return result;
    } else {
      return 0;
    }
  }

  @Override
  public Void visitVarDeclStat(VarDeclStatContext ctx) {
    visit(ctx.assignRhs());
    int stackPointerOffset = currentStackPointer
        - st.lookup(ctx.ident().getText());
    String msg = "[sp]";
    if (stackPointerOffset > 0) {
      msg = "[sp, #" + stackPointerOffset + "]";
    }
    if (ctx.type().getText().equals("int") || ctx.type().arrayType() != null
        || ctx.type().getText().equals("string")) {
      writer.addInst(Inst.STR, "r4, " + msg);
    } else {
      writer.addInst(Inst.STRB, "r4, " + msg);

    }
    return null;
  }

}
