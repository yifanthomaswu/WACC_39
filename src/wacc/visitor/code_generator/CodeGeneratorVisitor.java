package wacc.visitor.code_generator;

import antlr.*;
import antlr.BasicParser.*;
import wacc.visitor.SymbolTable;
import wacc.visitor.type.*;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;
  private SymbolTable st;
  private int sp;
  private Reg reg;
  private int numberOfPushes;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
    this.sp = 0;
    this.reg = Reg.R4;
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
    buildStat(ctx.stat());
    writer.addInst(Inst.LDR, "r0, =0");
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();
    return null;
  }

  private void buildStat(StatContext ctx) {
    int size = -initStack(ctx);
    subSP(size);
    visit(ctx);
    addSP(size);
    sp += size;
  }

  private void buildStatInNewScope(StatContext ctx) {
    st = new SymbolTable(st);
    buildStat(ctx);
    st = st.getEncSymTable();
  }

  private int initStack(StatContext ctx) {
    if (ctx instanceof VarDeclStatContext) {
      int offset = -getSize(Utils.getType(((VarDeclStatContext) ctx).type()));
      sp += offset;
      st.add(((VarDeclStatContext) ctx).ident().getText(), sp);
      return offset;
    } else if (ctx instanceof CompStatContext) {
      int offset = 0;
      for (StatContext c : ((CompStatContext) ctx).stat()) {
        offset += initStack(c);
      }
      return offset;
    } else {
      return 0;
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
        size -= 1024;
      }
      writer.addInst(Inst.SUB, "sp, sp, #" + size);
    }
  }

  private void addSP(int size) {
    if (size > 0) {
      while (size > 1024) {
        writer.addInst(Inst.ADD, "sp, sp, #1024");
        size -= 1024;
      }
      writer.addInst(Inst.ADD, "sp, sp, #" + size);
    }
  }

  @Override
  public Void visitFunc(FuncContext ctx) {
    st = new SymbolTable(st);
    if (ctx.paramList() != null) {
      visit(ctx.paramList());
    }

    writer.addLabel("f_" + ctx.ident().getText());
    writer.addInst(Inst.PUSH, "{lr}");
    buildStat(ctx.stat());
    writer.addInst(Inst.POP, "{pc}");
    writer.addLtorg();

    st = st.getEncSymTable();
    return null;
  }

  @Override
  public Void visitParamList(ParamListContext ctx) {
    int offset = 4;
    for (ParamContext c : ctx.param()) {
      String ident = c.ident().getText();
      st.add(ident, offset);
      st.add(ident, c.type());
      offset += getSize(Utils.getType(c.type()));
    }
    return null;
  }

  @Override
  public Void visitVarDeclStat(VarDeclStatContext ctx) {
    visit(ctx.assignRhs());
    String ident = ctx.ident().getText();
    st.add(ident, ctx.type());
    int offset = st.lookupI(ident) - sp;
    store(Utils.getType(ctx.type()), offset, "r4", "sp");
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
  public Void visitFreeStat(FreeStatContext ctx) {
    visit(ctx.expr());
    writer.addInst(Inst.MOV, "r0, r4");
    if (Utils.getType(ctx.expr(), st) instanceof PairType) {
      writer.addInst(Inst.BL, writer.p_free_pair());
    } else {
      writer.addInst(Inst.BL, writer.p_free_array());
    }
    return null;
  }

  @Override
  public Void visitReturnStat(ReturnStatContext ctx) {
    visit(ctx.expr());
    writer.addInst(Inst.MOV, "r0, r4");
    addSP(sp);
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
    String[] lpair = writer.getLabelLPair();
    writer.addInst(Inst.BEQ, lpair[0]);

    buildStatInNewScope(ctx.stat(0));

    writer.addInst(Inst.B, lpair[1]);
    writer.addLabel(lpair[0]);

    buildStatInNewScope(ctx.stat(1));

    writer.addLabel(lpair[1]);
    return null;
  }

  @Override
  public Void visitWhileStat(WhileStatContext ctx) {
    String[] lpair = writer.getLabelLPair();
    writer.addInst(Inst.B, lpair[0]);
    writer.addLabel(lpair[1]);

    buildStatInNewScope(ctx.stat());

    writer.addLabel(lpair[0]);
    visit(ctx.expr());
    writer.addInst(Inst.CMP, "r4, #1");
    writer.addInst(Inst.BEQ, lpair[1]);
    return null;
  }

  @Override
  public Void visitScopingStat(ScopingStatContext ctx) {
    buildStatInNewScope(ctx.stat());
    return null;
  }

  @Override
  public Void visitLhsIdent(LhsIdentContext ctx) {
    String ident = ctx.ident().getText();
    int offset = st.lookupAllI(ident) - sp;
    if (ctx.getParent() instanceof AssignStatContext) {
      store(Utils.getType(st.lookupAllT(ident)), offset, "r4", "sp");
    } else {
      writer.addInst(Inst.ADD, "r4, sp, #" + offset);
    }
    return null;
  }

  @Override
  public Void visitRhsNewPair(RhsNewPairContext ctx) {
    writer.addInst(Inst.LDR, "r0, =8");
    writer.addInst(Inst.BL, "malloc");
    writer.addInst(Inst.MOV, "r4, r0");

    reg = reg.next();
    for (int i = 0; i < ctx.expr().size(); i++) {
      visit(ctx.expr(i));
      Type type = Utils.getType(ctx.expr(i), st);
      writer.addInst(Inst.LDR, "r0, =" + getSize(type));
      writer.addInst(Inst.BL, "malloc");
      store(type, 0, "r5", "r0");
      if (i == 0) {
        writer.addInst(Inst.STR, "r0, [r4]");
      } else {
        writer.addInst(Inst.STR, "r0, [r4, #4]");
      }
    }
    reg = reg.previous();
    return null;
  }

  @Override
  public Void visitRhsCall(RhsCallContext ctx) {
    if (ctx.argList() != null) {
      visit(ctx.argList());
    }
    String ident = ctx.ident().getText();
    writer.addInst(Inst.BL, "f_" + ident);
    int size = paramSize(st.lookupAllF(ident));
    addSP(size);
    sp += size;
    writer.addInst(Inst.MOV, "r4, r0");
    return null;
  }

  private static int paramSize(FuncContext ctx) {
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

  @Override
  public Void visitArgList(ArgListContext ctx) {
    for (int i = ctx.expr().size() - 1; i >= 0; i--) {
      visit(ctx.expr(i));
      int size = getSize(Utils.getType(ctx.expr(i), st));
      if (size == 1) {
        writer.addInst(Inst.STRB, "r4, [sp, #-1]!");
      } else {
        writer.addInst(Inst.STR, "r4, [sp, #-4]!");
      }
      sp -= size;
    }
    return null;
  }

  @Override
  public Void visitPairElem(PairElemContext ctx) {
    boolean isRead = ctx.getParent() instanceof AssignRhsContext;
    reg = isRead ? reg : reg.next();
    visit(ctx.expr()); // puts res of expr in reg
    writer.addInst(Inst.MOV, "r0, " + reg);
    writer.addInst(Inst.BL, writer.p_check_null_pointer());
    if (ctx.FST() != null) {
      // get the first elem, offset = 0
      writer.addInst(Inst.LDR, reg + ", [" + reg + "]");
    } else {
      // get the second elem, offset = 4
      writer.addInst(Inst.LDR, reg + ", [" + reg + ", #4]");
    }
    Type type = Utils.getType(ctx, st);
    if (isRead) {
      load(type, 0, "r4", reg.toString());
    } else {
      store(type, 0, "r4", reg.toString());
    }
    reg = isRead ? reg : reg.previous();
    return null;
  }

  @Override
  public Void visitIntLiter(IntLiterContext ctx) {
    writer.addInst(Inst.LDR, reg + ", =" + Integer.parseInt(ctx.getText()));
    return null;
  }

  @Override
  public Void visitBoolLiter(BoolLiterContext ctx) {
    if (ctx.getText().equals("true")) {
      writer.addInst(Inst.MOV, reg + ", #1");
    } else {
      writer.addInst(Inst.MOV, reg + ", #0");
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
    writer.addInst(Inst.MOV, reg + ", #" + c);
    return null;
  }

  @Override
  public Void visitStringLiter(StringLiterContext ctx) {
    String s = ctx.getText();
    String msg = writer.addMsg(s.substring(1, s.length() - 1));
    writer.addInst(Inst.LDR, reg + ", =" + msg);
    return null;
  }

  @Override
  public Void visitPairLiter(PairLiterContext ctx) {
    writer.addInst(Inst.LDR, reg + ", =0");
    return null;
  }

  @Override
  public Void visitIdentExpr(IdentExprContext ctx) {
    String ident = ctx.ident().getText();
    int offset = st.lookupAllI(ident) - sp;
    load(Utils.getType(st.lookupAllT(ident)), offset, reg.toString(), "sp");
    return null;
  }

  private Void visitBinOpExprChildren(ExprContext expr1, ExprContext expr2) {

    visit(expr1);

    if (reg == Reg.R10) {
      writer.addInst(Inst.PUSH, "{r10}");
      numberOfPushes++;
    } else {
      reg = reg.next();
    }

    visit(expr2);

    if (reg == Reg.R10 && numberOfPushes > 0) {
      writer.addInst(Inst.POP, "{r11}");
      numberOfPushes--;
    } else {
      reg = reg.previous();
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec1Expr(BinOpPrec1ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = reg.next();

    if (ctx.MULT() != null) {
      writer.addInst(Inst.SMULL, reg + ", " + nextReg + ", " + reg + ", " + nextReg);
      writer.addInst(Inst.CMP, nextReg + ", " + reg + ", ASR #31");
      writer.addInst(Inst.BLNE, writer.p_throw_overflow_error());
    } else {
      writer.addInst(Inst.MOV, "r0, " + reg);
      writer.addInst(Inst.MOV, "r1, " + nextReg);
      writer.addInst(Inst.BL, writer.p_check_divide_by_zero());
      if (ctx.DIV() != null) {
        writer.addInst(Inst.BL, "__aeabi_idiv");
        writer.addInst(Inst.MOV, reg + ", r0");
      } else { // ctx.MOD() != null case
        writer.addInst(Inst.BL, "__aeabi_idivmod");
        writer.addInst(Inst.MOV, reg + ", r1");
      }
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec2Expr(BinOpPrec2ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = reg.next();

    if (ctx.PLUS() != null) {
      writer.addInst(Inst.ADDS, reg + ", " + reg + ", " + nextReg);
    } else {
      writer.addInst(Inst.SUBS, reg + ", " + reg + ", " + nextReg);
    }
    writer.addInst(Inst.BLVS, writer.p_throw_overflow_error());
    return null;
  }

  @Override
  public Void visitBinOpPrec3Expr(BinOpPrec3ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = reg.next();

    writer.addInst(Inst.CMP, reg + ", " + nextReg);
    if (ctx.GRT() != null) {
      writer.addInst(Inst.MOVGT, reg + ", #1");
      writer.addInst(Inst.MOVLE, reg + ", #0");
    } else if (ctx.GRT_EQUAL() != null) {
      writer.addInst(Inst.MOVGE, reg + ", #1");
      writer.addInst(Inst.MOVLT, reg + ", #0");
    } else if (ctx.LESS() != null) {
      writer.addInst(Inst.MOVLT, reg + ", #1");
      writer.addInst(Inst.MOVGE, reg + ", #0");
    } else {
      writer.addInst(Inst.MOVLE, reg + ", #1");
      writer.addInst(Inst.MOVGT, reg + ", #0");
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec4Expr(BinOpPrec4ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = reg.next();

    writer.addInst(Inst.CMP, reg + ", " + nextReg);
    if (ctx.EQUAL() != null) {
      writer.addInst(Inst.MOVEQ, reg + ", #1");
      writer.addInst(Inst.MOVNE, reg + ", #0");
    } else {
      writer.addInst(Inst.MOVNE, reg + ", #1");
      writer.addInst(Inst.MOVEQ, reg + ", #0");
    }
    return null;
  }

  @Override
  public Void visitBinOpPrec5Expr(BinOpPrec5ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = reg.next();
    writer.addInst(Inst.AND, reg + ", " + reg + ", " + nextReg);
    return null;
  }

  @Override
  public Void visitBinOpPrec6Expr(BinOpPrec6ExprContext ctx) {
    visitBinOpExprChildren(ctx.expr(0), ctx.expr(1));
    Reg nextReg = reg.next();
    writer.addInst(Inst.ORR, reg + ", " + reg + ", " + nextReg);
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

  @Override
  public Void visitArrayLiter(ArrayLiterContext ctx) {
    Type type;
    int size;
    if (ctx.expr().size() != 0) {
      type = Utils.getType(ctx.expr(0), st);
      size = getSize(type);
    } else {
      type = null;
      size = 0;
    }
    int offset = 4;

    writer.addInst(Inst.LDR, "r0, =" + (ctx.expr().size() * size + offset));
    writer.addInst(Inst.BL, "malloc");
    writer.addInst(Inst.MOV, reg + ", r0");

    Reg previousReg = reg;
    reg = reg.next();
    for (ExprContext c : ctx.expr()) {
      visit(c);
      store(type, offset, reg.toString(), previousReg.toString());
      offset += size;
    }

    writer.addInst(Inst.LDR, reg + ", =" + ctx.expr().size());
    writer.addInst(Inst.STR, reg + ", [" + previousReg + "]");
    reg = reg.previous();
    return null;
  }

  @Override
  public Void visitArrayElem(ArrayElemContext ctx) {
    boolean isRead = ctx.getParent() instanceof ArrayElemExprContext;
    Reg previousReg = isRead ? reg : reg.next();
    reg = isRead ? reg.next() : reg.next().next();

    String ident = ctx.ident().getText();
    int offset = st.lookupAllI(ident) - sp;
    writer.addInst(Inst.ADD, previousReg + ", sp, #" + offset);

    Type type = Utils.getType(ctx, st);
    int level = ((ArrayType) Utils.getType(ctx.ident(), st)).getLevel();
    for (int i = 0; i < ctx.expr().size(); i++) {
      visit(ctx.expr(i));
      writer.addInst(Inst.LDR, previousReg + ", [" + previousReg + "]");
      writer.addInst(Inst.MOV, "r0, " + reg);
      writer.addInst(Inst.MOV, "r1, " + previousReg);
      writer.addInst(Inst.BL, writer.p_check_array_bounds());
      writer.addInst(Inst.ADD, previousReg + ", " + previousReg + ", #4");
      if (i < level - 1 || getSize(type) == 4) {
        writer.addInst(Inst.ADD, previousReg + ", " + previousReg + ", " + reg + ", LSL #2");
      } else {
        writer.addInst(Inst.ADD, previousReg + ", " + previousReg + ", " + reg);
      }
    }

    reg = reg.previous();
    if (isRead) {
      load(type, 0, "r4", reg.toString());
    } else {
      store(type, 0, "r4", reg.toString());
      reg = reg.previous();
    }
    return null;
  }

  private void store(Type type, int offset, String rd, String rn) {
    Inst inst;
    if (getSize(type) == 1) {
      inst = Inst.STRB;
    } else {
      inst = Inst.STR;
    }
    storeAndLoadHelper(inst, offset, rd, rn);
  }

  private void load(Type type, int offset, String rd, String rn) {
    Inst inst;
    if (getSize(type) == 1) {
      inst = Inst.LDRSB;
    } else {
      inst = Inst.LDR;
    }
    storeAndLoadHelper(inst, offset, rd, rn);
  }

  private void storeAndLoadHelper(Inst inst, int offset, String rd, String rn) {
    if (offset == 0) {
      writer.addInst(inst, rd + ", [" + rn + "]");
    } else {
      writer.addInst(inst, rd + ", [" + rn + ", #" + offset + "]");
    }
  }

}
