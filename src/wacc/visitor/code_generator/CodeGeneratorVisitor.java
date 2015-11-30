package wacc.visitor.code_generator;

import java.util.HashMap;
import java.util.Map;

import antlr.*;
import antlr.BasicParser.*;
import wacc.visitor.semantic_error.utils.BaseLiter;
import wacc.visitor.semantic_error.utils.Utils;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private final Map<String, Object> st;
  private String currentReg; //turn to enum later, exploit ++ operator

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.st = new HashMap<String, Object>();
    currentReg = "r4";
  }
  
  @Override
  public Void visitProgram(ProgramContext ctx) {
    for (FuncContext c : ctx.func()) {
      visit(c);
    }
    writer.addLable("main");
    writer.addInst(Inst.PUSH, "{lr}");
    visit(ctx.stat());
    writer.addInst(Inst.LDR, "r0, =0");
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();
    return null;
  }  

  @Override
  public Void visitVarDeclStat(BasicParser.VarDeclStatContext ctx) {
    st.put(ctx.ident().getText(), st.size());
    writer.addInst(Inst.SUB, "sp, sp, #" + st.size());
    visit(ctx.assignRhs());
    currentReg = "r4";
    if (Utils.isSameBaseType(Utils.getType(ctx.type()), BaseLiter.INT)
            || false) // THis need to check if string
      writer.addInst(Inst.STR, "r4, [sp]");
    else
      writer.addInst(Inst.STRB, "r4, [sp]");
    writer.addInst(Inst.ADD, "sp, sp, #" + st.size());
    return null;
  }
  
  @Override
  public Void visitBoolLiter(BasicParser.BoolLiterContext ctx) {
    if(ctx.getText().equals("true"))
      writer.addInst(Inst.MOV, "r4, #1");
    else
      writer.addInst(Inst.MOV, "r4, #0");
    return null;
  }
  
  @Override
  public Void visitFunc(FuncContext ctx) {
    writer.addLable("f_" + ctx.ident().getText());
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
  public Void visitIntExpr(BasicParser.IntExprContext ctx) {
    writer.addInst(Inst.LDR, currentReg + ", =" + ctx.getText());
    currentReg = "r5";
    return null;
  }

  @Override
  public Void visitCharLiter(BasicParser.CharLiterContext ctx) {
    writer.addInst(Inst.MOV, "r4, #" + ctx.getText());
    return null;
  }

  @Override
  public Void visitBinOpPrec2Expr(BasicParser.BinOpPrec2ExprContext ctx) {
    visitChildren(ctx);
    if (ctx.PLUS() != null)
      writer.addInst(Inst.ADDS, "r4, r4, r5");
    else
      writer.addInst(Inst.SUBS, "r4, r4, r5");
    return null;
  }

}
