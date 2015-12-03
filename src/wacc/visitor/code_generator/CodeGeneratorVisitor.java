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
  private Regs currentReg; //turn to enum later, exploit ++ operator
  private int currentStackPointer = 0;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.st = new HashMap<String, Object>();
    currentReg = Regs.r4;
  }
  
  @Override
  public Void visitProgram(ProgramContext ctx) {
    for (FuncContext c : ctx.func()) {
      visit(c);
    }
    writer.addLabel("main");
    writer.addInst(Inst.PUSH, "{lr}");
    visit(ctx.stat());
    writer.addInst(Inst.LDR, "r0, =0");
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();
    return null;
  }  

//  @Override
//  public Void visitVarDeclStat(BasicParser.VarDeclStatContext ctx) {
//    st.put(ctx.ident().getText(), st.size());
//    int sizeOfVars = 0;
//    if (ctx.getParent() instanceof BasicParser.CompStatContext)
//    {
//      if (((CompStatContext) ctx.getParent()).stat(1) instanceof BasicParser.VarDeclStatContext)
//      {
//
//      }
//    }
//    writer.addInst(Inst.SUB, "sp, sp, #" + st.size());
//    visit(ctx.assignRhs());
//    currentReg = Regs.r4;
//    if (Utils.isSameBaseType(Utils.getType(ctx.type()), BaseLiter.INT)
//            || false) // THis need to check if string
//      writer.addInst(Inst.STR, "r4, [sp]");
//    else
//      writer.addInst(Inst.STRB, "r4, [sp]");
//    writer.addInst(Inst.ADD, "sp, sp, #" + st.size());
//    return null;
//  }
  
  
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
  public Void visitIntExpr(BasicParser.IntExprContext ctx) {
    writer.addInst(Inst.LDR, currentReg + ", =" + ctx.getText());
    currentReg = Regs.r5;
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

  @Override
  public Void visitBinOpPrec5Expr(BasicParser.BinOpPrec5ExprContext ctx) {
    visitChildren(ctx);
    writer.addInst(Inst.AND, "r4, r4, r5");
    return null;
  }

//@Override
//public Void visitCompStat(BasicParser.CompStatContext ctx) {
//	  int count = 0;
//	  while(ctx.getChild(count) instanceof VarDeclStatContext) {
//		  count++;
//	  }
//	  visit
//}

  @Override
  public Void visitVarDeclStat(BasicParser.VarDeclStatContext ctx) {
	switch(ctx.type().getText()) {
	case("bool"):
	case("char"):
		currentStackPointer++;
		break;
	case("int"):
	case("string"):
		currentStackPointer += 4;
		break;
	}
	st.put(ctx.ident().getText(), currentStackPointer);
	return visitChildren(ctx);
}

@Override
public Void visitBoolLiter(BasicParser.BoolLiterContext ctx) {
	  if(ctx.getText().equals("true")) {
		  writer.addInst(Inst.MOV, currentReg + ", #1");
	  } else {
		  writer.addInst(Inst.MOV, currentReg + ", #0");
	  }
	  if(st.size() < 1) {
		  writer.addInst(Inst.STRB, currentReg + ", [sp]");
	  } else {
		  writer.addInst(Inst.STRB, currentReg + ", [sp, #" + (st.size()-1) + "]");
	  }
	  
	  return null;
 }

//@Override
//public Void visitBoolLiter(BasicParser.BoolLiterContext ctx) {
//  if(ctx.getText().equals("true"))
//    writer.addInst(Inst.MOV, currentReg + ", #1");
//  else
//    writer.addInst(Inst.MOV, currentReg + ", #0");
//  currentReg = Regs.r5;
//  return null;
//}
}
