package wacc.visitor;

import java.util.Map;

import antlr.*;
import antlr.BasicParser.VarDeclStatContext;
import wacc.visitor.utils.CodeWriter;
import wacc.visitor.utils.Inst;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private final Map<String, Object> st;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
  }
  
  public String visitBegin() {
      
  }
  
  @Override
  public Void visitProgram(BasicParser.ProgramContext ctx) {
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
  


}
