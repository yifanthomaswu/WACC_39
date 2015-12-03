package wacc.visitor.code_generator;

import antlr.*;
import antlr.BasicParser.*;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private SymbolTable st;
  private Regs currentReg; // turn to enum later, exploit ++ operator

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.currentReg = Regs.r4;
  }

  @Override
  public Void visitProgram(ProgramContext ctx) {
    st = new SymbolTable(null);
    for (FuncContext c : ctx.func()) {
      visit(c);
    }
    writer.addLabel("main");
    writer.addInst(Inst.PUSH, "{lr}");
    visit(ctx.stat());
    writer.addInst(Inst.LDR, "r0, =0");
    writer.addInst(Inst.POP, "{pc}");
    ;
    writer.addLtorg();
    return null;
  }

  // @Override
  // public Void visitVarDeclStat(VarDeclStatContext ctx) {
  // st.put(ctx.ident().getText(), st.size());
  // int sizeOfVars = 0;
  // if (ctx.getParent() instanceof CompStatContext)
  // {
  // if (((CompStatContext) ctx.getParent()).stat(1) instanceof
  // VarDeclStatContext)
  // {
  //
  // }
  // }
  // writer.addInst(Inst.SUB, "sp, sp, #" + st.size());
  // visit(ctx.assignRhs());
  // currentReg = Regs.r4;
  // if (Utils.isSameBaseType(Utils.getType(ctx.type()), BaseLiter.INT)
  // || false) // THis need to check if string
  // writer.addInst(Inst.STR, "r4, [sp]");
  // else
  // writer.addInst(Inst.STRB, "r4, [sp]");
  // writer.addInst(Inst.ADD, "sp, sp, #" + st.size());
  // return null;
  // }

  @Override
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
  public Void visitBoolLiter(BoolLiterContext ctx) {
    if (ctx.getText().equals("true"))
      writer.addInst(Inst.MOV, currentReg + ", #1");
    else
      writer.addInst(Inst.MOV, currentReg + ", #0");
    currentReg = Regs.r5;
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

  @Override
  public Void visitIntExpr(IntExprContext ctx) {
    writer.addInst(Inst.LDR, currentReg + ", =" + ctx.getText());
    currentReg = Regs.r5;
    return null;
  }

  @Override
  public Void visitCharLiter(CharLiterContext ctx) {
    writer.addInst(Inst.MOV, "r4, #" + ctx.getText());
    return null;
  }

//  @Override
//  public Void visitBinOpPrec1Expr(BinOpPrec1ExprContext ctx) {
//    if (ctx.MULT() != null) {
//      writer.
//    }
//    return null;
//  }

  @Override
  public Void visitBinOpPrec2Expr(BinOpPrec2ExprContext ctx) {
    visitChildren(ctx);
    if (ctx.PLUS() != null) {
      writer.addInst(Inst.ADDS, "r4, r4, r5");
    } else {
      writer.addInst(Inst.SUBS, "r4, r4, r5");
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
    writer.addInst(Inst.OR, "r4, r4, r5");
    return null;
  }

}
