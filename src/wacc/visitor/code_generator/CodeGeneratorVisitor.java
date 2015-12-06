package wacc.visitor.code_generator;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.*;
import antlr.BasicParser.*;
import wacc.visitor.SymbolTable;
import wacc.visitor.type.*;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private SymbolTable st;
  private int currentSP;
  private Reg currentReg;
  private int numberOfPushes;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.currentSP = 0;
    this.currentReg = Reg.R4;
    this.numberOfPushes = 0;
  }

  @Override
  public Void visitProgram(ProgramContext ctx) {
    st = new SymbolTable(null);
    for (FuncContext c : ctx.func()) {
      st.add(c.ident().getText(), c);
    }
    for (FuncContext c : ctx.func()) {
      visit(c);
    }
    writer.addLabel("main");
    writer.addInst(Inst.PUSH, "{lr}");
    int size = stackSize(ctx.stat());
    subSP(size);
    visit(ctx.stat());
    addSP(size);
    writer.addInst(Inst.LDR, "r0, =0");
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();
    return null;
  }

  @Override
  public Void visitFunc(FuncContext ctx) {
    st = new SymbolTable(st);
    int tempSP = currentSP;
    currentSP = 0;
    writer.addLabel("f_" + ctx.ident().getText());
    writer.addInst(Inst.PUSH, "{lr}");
    visit(ctx.paramList());
    int size = stackSize(ctx.stat());
    subSP(size);
    visit(ctx.stat());
    addSP(size);
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();

    currentSP = tempSP;
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitParamList(ParamListContext ctx) {
    int offset = -4;
    for (ParamContext c : ctx.param()) {
      String ident = c.ident().getText();
      st.add(ident, offset);
      st.add(ident, c.type());
      offset -= getSize(Utils.getType(c.type()));
    }
    return null;
  }

  private int stackSize(StatContext ctx) {
    if (ctx instanceof VarDeclStatContext) {
      int size = getSize(Utils.getType(((VarDeclStatContext) ctx).type()));
      currentSP += size;
      st.add(((VarDeclStatContext) ctx).ident().getText(), currentSP);
      return size;
    } else if (ctx instanceof CompStatContext) {
      int size = 0;
      for (StatContext c : ((CompStatContext) ctx).stat()) {
        size += stackSize(c);
      }
      return size;
    } else {
      return 0;
    }
  }

  private static int stackSize(FuncContext ctx) {
    if (ctx.paramList() == null) {
      return 0;
    } else {
      int size = 0;
      for (ParamContext c : ctx.paramList().param()) {
        size += getSize(Utils.getType(c.type()));
      }
      return size;
    }
  }

  private static int getSize(Type type) {
    if (Utils.isSameBaseType(type, BaseLiter.BOOL)
        || Utils.isSameBaseType(type, BaseLiter.CHAR)) {
      return 1;
    } else {
      return 4;
    }
  }

  private void subSP(int size) {
    if (size > 0) {
      while (size > 1024) {
        writer.addInst(Inst.SUB, "sp, sp, #1024");
        size %= 1024;
      }
      writer.addInst(Inst.SUB, "sp, sp, #" + size);
    }
  }

  private void addSP(int size) {
    if (size > 0) {
      while (size > 1024) {
        writer.addInst(Inst.ADD, "sp, sp, #1024");
        size %= 1024;
      }
      writer.addInst(Inst.ADD, "sp, sp, #" + size);
    }
  }

  @Override
  public Void visitVarDeclStat(VarDeclStatContext ctx) {
    String ident = ctx.ident().getText();
    st.add(ident, ctx.type());
    visit(ctx.assignRhs());
    Inst inst;
    if (getSize(Utils.getType(ctx.type())) == 1) {
      inst = Inst.STRB;
    } else {
      inst = Inst.STR;
    }
    int offset = currentSP - st.lookupI(ident);
    if (offset == 0) {
      writer.addInst(inst, "r4, [sp]");
    } else {
      writer.addInst(inst, "r4, [sp, #" + offset + "]");
    }
    return null;
  }

  @Override
  public Void visitAssignStat(AssignStatContext ctx) {
    visit(ctx.assignRhs());
    visit(ctx.assignLhs());
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
    int perviousSP0 = currentSP;
    currentSP = 0;
    int size0 = stackSize(ctx.stat(0));
    subSP(size0);
    visit(ctx.stat(0));
    addSP(size0);
    currentSP = perviousSP0;
    st = st.getEncSymTable();

    writer.addInst(Inst.B, pair[1]);
    writer.addLabel(pair[0]);

    st = new SymbolTable(st);
    int perviousSP1 = currentSP;
    currentSP = 0;
    int size1 = stackSize(ctx.stat(1));
    subSP(size1);
    visit(ctx.stat(1));
    addSP(size1);
    currentSP = perviousSP1;
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
    int perviousSP = currentSP;
    currentSP = 0;
    int size = stackSize(ctx.stat());
    subSP(size);
    visit(ctx.stat());
    addSP(size);
    currentSP = perviousSP;
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
    int perviousSP = currentSP;
    currentSP = 0;
    int size = stackSize(ctx.stat());
    subSP(size);
    visit(ctx.stat());
    addSP(size);
    currentSP = perviousSP;
    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitRhsCall(RhsCallContext ctx) {
    visit(ctx.argList());
    String ident = ctx.ident().getText();
    writer.addInst(Inst.BL, "f_" + ident);
    int size = stackSize(st.lookupAllF(ident));
    currentSP -= size;
    addSP(size);
    writer.addInst(Inst.MOV, "r4, r0");
    return null;
  }

  @Override
  public Void visitArgList(ArgListContext ctx) {
    for (int i = ctx.expr().size() - 1; i >= 0; i--) {
      visit(ctx.expr(i));
      int size = getSize(Utils.getType(ctx.expr(i), st));
      currentSP += size;
      if (size == 1) {
        writer.addInst(Inst.STRB, "r4, [sp, #-1]!");
      } else {
        writer.addInst(Inst.STR, "r4, [sp, #-4]!");
      }
    }
    return null;
  }

  private Void visitBinOpExprChildren(ExprContext expr1, ExprContext expr2) {

    visit(expr1);

    if (currentReg == Reg.R10) {
      writer.addInst(Inst.PUSH, "{r10}");
      numberOfPushes++;
    } else {
      currentReg = Reg.values()[currentReg.ordinal() + 1];
    }

    visit(expr2);

    if (currentReg == Reg.R10 && numberOfPushes > 0) {
      writer.addInst(Inst.POP, "{r11}");
      numberOfPushes--;
    } else {
      currentReg = Reg.values()[currentReg.ordinal() - 1];
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec1Expr(BinOpPrec1ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = Reg.values()[currentReg.ordinal() + 1];

    if (ctx.MULT() != null) {
      writer.addInst(Inst.SMULL, currentReg + ", " + nextReg + ", " + currentReg + ", " + nextReg);
      writer.addInst(Inst.CMP, nextReg + ", " + currentReg + ", ASR #31");
      writer.addInst(Inst.BLNE, writer.p_throw_overflow_error());
    } else {
      writer.addInst(Inst.MOV, "r0, " + currentReg);
      writer.addInst(Inst.MOV, "r1, " + nextReg);
      writer.addInst(Inst.BL, writer.p_check_divide_by_zero());
      if (ctx.DIV() != null) {
        writer.addInst(Inst.BL, "__aeabi_idiv");
        writer.addInst(Inst.MOV, currentReg + ", r0");
      } else { // ctx.MOD() != null case
        writer.addInst(Inst.BL, "__aeabi_idivmod");
        writer.addInst(Inst.MOV, currentReg + ", r1");
      }
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec2Expr(BinOpPrec2ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = Reg.values()[currentReg.ordinal() + 1];
    Inst instr;
    if (ctx.PLUS() != null) {
      instr = Inst.ADDS;
    } else {
      instr = Inst.SUBS;
    }

    if (currentReg == Reg.R10) {
      writer.addInst(instr, "r10, r11, r10");
    } else {
      writer.addInst(instr, currentReg + ", " + currentReg + ", " + nextReg);
    }

    writer.addInst(Inst.BLVS, writer.p_throw_overflow_error());
    return null;
  }

  @Override
  public Void visitBinOpPrec3Expr(BinOpPrec3ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = Reg.values()[currentReg.ordinal() + 1];

    writer.addInst(Inst.CMP, currentReg + ", " + nextReg);
    if (ctx.GRT() != null) {
      writer.addInst(Inst.MOVGT, currentReg + ", #1");
      writer.addInst(Inst.MOVLE, currentReg + ", #0");
    } else if (ctx.GRT_EQUAL() != null) {
      writer.addInst(Inst.MOVGE, currentReg + ", #1");
      writer.addInst(Inst.MOVLT, currentReg + ", #0");
    } else if (ctx.LESS() != null) {
      writer.addInst(Inst.MOVLT, currentReg + ", #1");
      writer.addInst(Inst.MOVGE, currentReg + ", #0");
    } else {
      writer.addInst(Inst.MOVLE, currentReg + ", #1");
      writer.addInst(Inst.MOVGT, currentReg + ", #0");
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec4Expr(BinOpPrec4ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = Reg.values()[currentReg.ordinal() + 1];

    writer.addInst(Inst.CMP, currentReg + ", " + nextReg);
    if (ctx.EQUAL() != null) {
      writer.addInst(Inst.MOVEQ, currentReg + ", #1");
      writer.addInst(Inst.MOVNE, currentReg + ", #0");
    } else {
      writer.addInst(Inst.MOVNE, currentReg + ", #1");
      writer.addInst(Inst.MOVEQ, currentReg + ", #0");
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec5Expr(BinOpPrec5ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = Reg.values()[currentReg.ordinal() + 1];
    writer.addInst(Inst.AND, currentReg + ", " + currentReg + ", " + nextReg);
    return null;
  }

  @Override
  public Void visitBinOpPrec6Expr(BinOpPrec6ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = Reg.values()[currentReg.ordinal() + 1];
    writer.addInst(Inst.ORR, currentReg + ", " + currentReg + ", " + nextReg);
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

  private void store(Type type, int offset, String reg1, String reg2) {
    String msg = "[" + reg2 + "]";
    if (offset > 0) {
      msg = "[" + reg2 + ", #" + offset + "]";
    }
    if (Utils.isSameBaseType(type, BaseLiter.CHAR)
            || Utils.isSameBaseType(type, BaseLiter.BOOL)) {
      writer.addInst(Inst.STRB, reg1 + ", " + msg);
    } else  {
      writer.addInst(Inst.STR, reg1 + ", " + msg);
    }
  }

  @Override
  public Void visitLhsIdent(LhsIdentContext ctx) {
    int stackPointerOffset = currentSP - st.lookupI(ctx.ident().getText());
    store(Utils.getType(ctx.ident(), st), stackPointerOffset, "r4", "sp");
    // TODO
    return null;
  }

  @Override
  public Void visitIdent(IdentContext ctx) {
    ParserRuleContext context = ctx.getParent();
    if (context.parent instanceof LhsArrayElemContext) {
      store(Utils.getType(ctx, st), 0, "r4", currentReg.toString());
    } else if (context.parent instanceof ArrayElemExprContext) {
      writer.addInst(Inst.LDR, "r4, [" + currentReg + "]");
    } else if (!(context instanceof FuncContext)
        && !(context instanceof RhsCallContext)
        && !(context instanceof ParamContext)) {
      int stackPointerOffset = currentSP - st.lookupI(ctx.getText());
      Type type = Utils.getType(st.lookupT(ctx.getText()));
      load(type, stackPointerOffset,  currentReg.toString(), "sp");
    }
    return null;
  }

  private void load(Type type, int stackPointerOffset, String reg1, String reg2) {
    String msg = "[" + reg2 + "]";
    if (stackPointerOffset > 0) {
      msg = "[" + reg2 + ", #" + stackPointerOffset + "]";
    }
    if (Utils.isSameBaseType(type, BaseLiter.CHAR)
            || Utils.isSameBaseType(type, BaseLiter.BOOL)){
      writer.addInst(Inst.LDRSB, reg1 + ", " + msg);
    } else {
      writer.addInst(Inst.LDR, reg1 + ", " + msg);
    }
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
  public Void visitArrayElem(ArrayElemContext ctx) {
    Reg previousReg = currentReg;
    currentReg = Reg.values()[currentReg.ordinal() + 1];
    if (ctx.parent instanceof LhsArrayElemContext) {
      previousReg = currentReg;
      currentReg = Reg.values()[currentReg.ordinal() + 1];
    }
    writer.addInst(Inst.ADD, previousReg + ", sp, #0");
    Type type = Utils.getType(ctx.ident(), st);
    String typeString = type.toString();
    for (int level = 0; level < ((ArrayType) type).getLevel(); level++) {
      visit(ctx.expr(level));
      typeString = typeString.substring(0, typeString.length() - 2);
      writeArrayElemInstructions(typeString, previousReg);
    }
    currentReg = Reg.values()[currentReg.ordinal() - 1];
    visit(ctx.ident());
    if (ctx.parent instanceof LhsArrayElemContext) {
      previousReg = currentReg;
      currentReg = Reg.values()[currentReg.ordinal() - 1];
    }
    return null;
  }

  private void writeArrayElemInstructions(String type, Reg previousReg) {
    writer.addInst(Inst.LDR, previousReg + ", [" + previousReg + "]");
    writer.addInst(Inst.MOV, "r0, " + currentReg);
    writer.addInst(Inst.MOV, "r1, " + previousReg);
    writer.addInst(Inst.BL, writer.p_check_array_bounds());
    writer.addInst(Inst.ADD, previousReg + ", " + previousReg + ", #4");

    if (type.endsWith("[]") || type.contains("INT")) {
      writer.addInst(Inst.ADD, previousReg + ", " + previousReg + ", " + currentReg + ", LSL #2");
    } else { // reached a BOOL or CHAR type
      writer.addInst(Inst.ADD, previousReg + ", " + previousReg + ", " + currentReg);
    }
  }

  @Override
  public Void visitRhsNewPair(RhsNewPairContext ctx) {
    writer.addInst(Inst.LDR, "r0, =8");
    writer.addInst(Inst.BL, "malloc");
    writer.addInst(Inst.MOV, "r4, r0");

    currentReg = Reg.R5;

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

    currentReg = Reg.R4;

    return null;
  }

  @Override
  public Void visitPairElem(PairElemContext ctx) {
    boolean right = ctx.getParent() instanceof AssignRhsContext;
    currentReg = right ? Reg.R4 : Reg.R5;
    visit(ctx.expr()); // puts res of expr in currentReg
    writer.addInst(Inst.MOV, "r0, " + currentReg);
    writer.addInst(Inst.BL, writer.p_check_null_pointer());
    Type type;
    if (ctx.FST() != null) {
      writer.addInst(Inst.LDR, currentReg + ", [" + currentReg + "]"); //get the first elem, offset = 0
      type = ((PairType)Utils.getType(ctx.expr(), st)).getElem(0); //get type of first pair
    }
    else {
      writer.addInst(Inst.LDR, currentReg + ", [" + currentReg + ", #4]"); //get the second elem, offset = 4
      type = ((PairType)Utils.getType(ctx.expr(), st)).getElem(1); //get type of second pair
    }
//    writer.addInst(Inst.LDR, "r4, [r4]");
    if (right)
      load(type, 0, "r4", "r4");
    else
      store(type, 0, "r4", currentReg.toString());

    currentReg = Reg.R4;
    return null;
  }
}
