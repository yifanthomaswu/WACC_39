package wacc.visitor.code_generator;

import java.util.HashMap;
import java.util.Map;

import antlr.*;
import antlr.BasicParser.*;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private final Map<String, Object> st;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.st = new HashMap<String, Object>();
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
	  return visitChildren(ctx);
  }
  
  @Override
  public Void visitBoolLiter(BasicParser.BoolLiterContext ctx) {
	  if(ctx.getText() == "true") {
		  writer.addInst(Inst.MOV, "r4, #1");
	  }
	  writer.addInst(Inst.MOV, "r4, #0");
	  writer.addInst(Inst.STRB, "r4, [sp]");
	  writer.addInst(Inst.ADD, "sp, sp, #1");
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

}
