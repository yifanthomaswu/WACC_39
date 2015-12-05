package wacc.visitor.code_generator;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.*;
import antlr.BasicParser.*;
import wacc.visitor.SymbolTable;
import wacc.visitor.type.*;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private SymbolTable st;
  private int currentStackPointer;
  private Reg currentReg;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.currentStackPointer = 0;
    this.currentReg = Reg.r4;
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
    writer.addLtorg();
    return null;
  }

  @Override
  public Void visitReadStat(ReadStatContext ctx) {
    visit(ctx.assignLhs());
    writer.addInst(Inst.MOV, "r0, r4");
    Type type = Utils.getType(ctx.assignLhs(), st);
    if (Utils.isSameBaseType(type, BaseLiter.INT)) {
      writer.addInst(Inst.BL, writer.p_read_int());
    } else {
      writer.addInst(Inst.BL, writer.p_read_char());
    }
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
  public Void visitPrintStat(PrintStatContext ctx) {
    visit(ctx.expr());
    printStatsHelper(Utils.getType(ctx.expr(), st));
    return null;
  }

  @Override
  public Void visitPrintlnStat(PrintlnStatContext ctx) {
    visit(ctx.expr());
    printStatsHelper(Utils.getType(ctx.expr(), st));
    writer.addInst(Inst.BL, writer.p_print_ln());
    return null;
  }

  private void printStatsHelper(Type type) {
    writer.addInst(Inst.MOV, "r0, r4");
    if (Utils.isSameBaseType(type, BaseLiter.INT)) {
      writer.addInst(Inst.BL, writer.p_print_int());
    } else if (Utils.isSameBaseType(type, BaseLiter.BOOL)) {
      writer.addInst(Inst.BL, writer.p_print_bool());
    } else if (Utils.isSameBaseType(type, BaseLiter.CHAR)) {
      writer.addInst(Inst.BL, "putchar");
    } else if (Utils.isStringType(type)) {
      writer.addInst(Inst.BL, writer.p_print_string());
    } else {
      writer.addInst(Inst.BL, writer.p_print_reference());
    }
  }

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
  public Void visitFunc(FuncContext ctx) {
    int temp = currentStackPointer;
    currentStackPointer = 0;
    st = new SymbolTable(st);
    writer.addLabel("f_" + ctx.ident().getText());
    writer.addInst(Inst.PUSH, "{lr}");
    visitChildren(ctx);
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();
    currentStackPointer = temp;
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitParamList(ParamListContext ctx) {
    int size = 0;
    for (int i = 0; i < ctx.param().size(); i++) {
      if(i == 0) {
    	  size -= 4;
      }
      st.add(ctx.param(i).ident().getText(), size);
      st.add(ctx.param(i).ident().getText(), ctx.param(i).type());
     if (ctx.param(i).type().baseType() != null) {
    	        switch (ctx.param(i).type().getText()) {
    	          case "bool":
    	          case "char":
    	            size--;
    	            break;
    	          case "int":
    	          case "string":
    	            size -= 4;
    	            break;
    	        }
    	  } else if (ctx.param(i).type().arrayType() != null) {
    	        size -= 4;
    	  } 
      }
      
    return null;
  }

  @Override
  public Void visitRhsCall(RhsCallContext ctx) {
	  visitChildren(ctx);
	  writer.addInst(Inst.BL, "f_" + ctx.ident().getText());
	  int size = 0;
	  for(ExprContext c : ctx.argList().expr()) {
		  Type t = Utils.getType(c,st);
		  if(Utils.isSameBaseType(t, BaseLiter.CHAR) ||
				  Utils.isSameBaseType(t, BaseLiter.BOOL)) {
			  size++;
		  } else {
			  size += 4;
		  } 
	  }
	  if(size >0) {
		  writer.addInst(Inst.ADD, "sp, sp, #" + size);
	  }
	  writer.addInst(Inst.MOV, "r4, r0");
	  return null;

  }
  
  @Override
  public Void visitArgList(ArgListContext ctx) {
	  if(ctx.expr().size() > 0) {
		  String msg = "[sp]";
		  int offset = 0;
		  Type t = Utils.getType(ctx.expr(0),st);
		  if(Utils.isSameBaseType(t, BaseLiter.CHAR) || 
				  Utils.isSameBaseType(t, BaseLiter.BOOL)) {
			  writer.addInst(Inst.LDRSB, "r4, " + msg);
			  offset++;
		  } else {
			  writer.addInst(Inst.LDR, "r4, " + msg);
			  offset += 4;
		  }
		  msg = "[sp, #-" + offset + "]!";
		  if(Utils.isSameBaseType(t, BaseLiter.CHAR) || 
				  Utils.isSameBaseType(t, BaseLiter.BOOL)) {
			  writer.addInst(Inst.STRB, "r4, " + msg);
		  } else {
			  writer.addInst(Inst.STR, "r4, " + msg);
		  }
	  } 
	  return null;
  }

  // private void sizeOfParam(ParamContext ctx, Integer size) {
  // if (ctx.type().baseType() != null) {
  // switch (ctx.type().getText()) {
  // case "bool":
  // case "char":
  // size++;
  // break;
  // case "int":
  // case "string":
  // size += 4;
  // break;
  // }
  // } else if (ctx.type().arrayType() != null) {
  // size += 4;
  // }
  // st.add(ctx.ident().getText(), size);
  // }

  @Override
  public Void visitParam(ParamContext ctx) {
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
    // visitChildren(ctx);
    visit(ctx.expr(0));
    currentReg = Reg.r5;
    visit(ctx.expr(1));
    writer.addInst(Inst.CMP, "r4, r5");
    if (ctx.EQUAL() != null) {
      writer.addInst(Inst.MOVEQ, "r4, #1");
      writer.addInst(Inst.MOVNE, "r4, #0");
    } else {
      writer.addInst(Inst.MOVNE, "r4, #1");
      writer.addInst(Inst.MOVEQ, "r4, #0");
    }
    currentReg = Reg.r4;
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

  @Override
  public Void visitUnOpExpr(UnOpExprContext ctx) {
    visit(ctx.expr());
    if (ctx.unaryOper().UNARY_OPER() != null) {
      String operator = ctx.unaryOper().UNARY_OPER().getText();
      if (operator.equals("len")) {
        // length of array stored as first elem in array, visiting expr will
        // put start of array into r4
        writer.addInst(Inst.LDR, "r4, [r4]");
      } else if (operator.equals("!")) {
        // negate r4, as this is value of evaluated bool expr
        writer.addInst(Inst.EOR, "r4, r4, #1");
      } else {
        // do nothing, chars treated as nums in ass
      }
    } else {
      // only minus left
      writer.addInst(Inst.RSBS, "r4, r4, #0");
      writer.addInst(Inst.BLVS, writer.p_throw_overflow_error());
    }
    return null;
  }

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
      } else {
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
    st.add(ctx.ident().getText(), ctx.type());
    visit(ctx.assignRhs());
    int stackPointerOffset = currentStackPointer - st.lookupI(ctx.ident().getText());
    store(Utils.getType(ctx.type()), stackPointerOffset, "r4", "sp");
    return null;
  }

  private void store(Type type, int stackPointerOffset, String reg1, String reg2) {
    String msg = "[" + reg2 + "]";
    if (stackPointerOffset > 0) {
      msg = "["+ reg2 + ", #" + stackPointerOffset + "]";
    }
    if (Utils.isSameBaseType(type, BaseLiter.INT) || type instanceof ArrayType
        || Utils.isStringType(type) || type instanceof PairType) {
      writer.addInst(Inst.STR, reg1 + ", " + msg);
    } else {
      writer.addInst(Inst.STRB, reg1 + ", " + msg);
    }
  }

  @Override
  public Void visitAssignStat(AssignStatContext ctx) {
    visit(ctx.assignRhs());
    visit(ctx.assignLhs());
    return null;
  }

  @Override
  public Void visitLhsIdent(LhsIdentContext ctx) {
    IdentContext ident = ctx.ident();
    String msg = "[sp]";
    int stackPointerOffset = currentStackPointer - st.lookupI(ident.getText());
    if (stackPointerOffset > 0) {
      msg = "[sp, #" + stackPointerOffset + "]";
    }
    Type type = Utils.getType(ident, st);
    if (Utils.isSameBaseType(type, BaseLiter.BOOL)
        || Utils.isSameBaseType(type, BaseLiter.CHAR)) {
      writer.addInst(Inst.STRB, "r4, " + msg);
    } else {
      writer.addInst(Inst.STR, "r4, " + msg);
    }// TODO
    return null;
  }

  @Override
  public Void visitIdent(IdentContext ctx) {
    ParserRuleContext context = ctx.getParent();
    if (!(context instanceof FuncContext)
        && !(context instanceof RhsCallContext) 
        && !(context instanceof ParamContext)) {
      String msg = "[sp]";
      int stackPointerOffset = currentStackPointer - st.lookupI(ctx.getText());
      if (stackPointerOffset > 0) {
        msg = "[sp, #" + stackPointerOffset + "]";
      }
      if (st.lookupT(ctx.getText()).getText().equals("int")
          || st.lookupT(ctx.getText()).arrayType() != null
          || st.lookupT(ctx.getText()).pairType() != null
          || st.lookupT(ctx.getText()).getText().equals("string")) {
        writer.addInst(Inst.LDR, currentReg + ", " + msg);
      } else {
        writer.addInst(Inst.LDRSB, currentReg + ", " + msg);
      }
    }
    return null;
  }

  @Override
  public Void visitIntLiter(IntLiterContext ctx) {
    writer.addInst(Inst.LDR,
        currentReg + ", =" + Integer.parseInt(ctx.getText()));
    return null;
  }

  @Override
  public Void visitBoolLiter(BoolLiterContext ctx) {
    if (ctx.getText().equals("true")) {
      writer.addInst(Inst.MOV, currentReg + ", #1");
    } else {
      writer.addInst(Inst.MOV, currentReg + ", #0");
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
      case '\\':
        c = "\'\\\'";
        break;
    }
    writer.addInst(Inst.MOV, currentReg + ", #" + c);
    return null;
  }

  @Override
  public Void visitStringLiter(StringLiterContext ctx) {
    String s = ctx.getText();
    String msg = writer.addMsg(s.substring(1, s.length() - 1));
    writer.addInst(Inst.LDR, currentReg + ", =" + msg);
    return null;
  }

  @Override
  public Void visitArrayLiter(ArrayLiterContext ctx) {
    int typeSize;
    int offset = 4;
    Inst instruction;

    if (ctx.expr(0) instanceof IntExprContext
        || ctx.expr(0) instanceof StringExprContext) {
      typeSize = 4;
      instruction = Inst.STR;
    } else if (ctx.expr(0) instanceof ArrayElemExprContext
        || ctx.expr(0) instanceof IdentExprContext) {

      Type type;
      if (ctx.expr(0) instanceof ArrayElemExprContext) {
        type = Utils.getType(((ArrayElemExprContext) ctx.expr(0)).arrayElem()
            .ident(), st);
      } else {
        type = Utils.getType(((IdentExprContext) ctx.expr(0)).ident(), st);
      }

      if (Utils.isSameBaseType(type, BaseLiter.INT)
          || type instanceof wacc.visitor.type.ArrayType) {
        typeSize = 4;
        instruction = Inst.STR;
      } else { // is a bool or char
        typeSize = 1;
        instruction = Inst.STRB;
      }

    } else { // is a bool or char
      typeSize = 1;
      instruction = Inst.STRB;
    }

    int spaceToSave = ctx.expr().size() * typeSize + 4;
    writer.addInst(Inst.LDR, "r0, =" + spaceToSave);
    writer.addInst(Inst.BL, "malloc");
    writer.addInst(Inst.MOV, currentReg + ", r0");

    Reg previousReg = currentReg;
    currentReg = Reg.values()[currentReg.ordinal() + 1];
    for (ExprContext expr : ctx.expr()) {
      visit(expr);
      writer.addInst(instruction, currentReg + ", [" + previousReg + ", #"
          + offset + "]");
      offset += typeSize;
    }

    writer.addInst(Inst.LDR, currentReg + ", =" + ctx.expr().size());
    writer.addInst(Inst.STR, currentReg + ", [" + previousReg + "]");
    currentReg = Reg.values()[currentReg.ordinal() - 1];
    return null;
  }

  @Override
  public Void visitRhsNewPair(BasicParser.RhsNewPairContext ctx) {
    writer.addInst(Inst.LDR, "r0, =8");
    writer.addInst(Inst.BL, "malloc");
    writer.addInst(Inst.MOV, "r4, r0");

    currentReg = Reg.r5;

    visit(ctx.expr(0));
    Type type0 = Utils.getType(ctx.expr(0), st);
    writer.addInst(Inst.LDR, "r0, =" + getSize(type0));
    writer.addInst(Inst.BL, "malloc");
    store(type0, 0, "r5", "r0");
    writer.addInst(Inst.STR, "r0, [r4]");

    visit(ctx.expr(1));
    Type type1 = Utils.getType(ctx.expr(1), st);
    writer.addInst(Inst.LDR, "r0, =" + getSize(type1));
    writer.addInst(Inst.BL, "malloc");
    store(type1, 0, "r5", "r0");
    writer.addInst(Inst.STR, "r0, [r4, #4]");

    currentReg = Reg.r4;

    return null;
  }

  private int getSize(Type type) {
    int size;
    if (Utils.isSameBaseType(type, BaseLiter.BOOL)
            || Utils.isSameBaseType(type, BaseLiter.CHAR))
      size = 1;
    else
    size = 4;
    return size;
  }

}
