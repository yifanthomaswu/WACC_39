package wacc.visitor.code_generator;

import java.io.PrintWriter;

public class CodeWriter {

  private final PrintWriter file;
  private final StringBuilder data;
  private final StringBuilder text;
  private final StringBuilder text_p;
  private static final String DATA_HEADER = "\t.data\n\n";
  private static final String TEXT_HEADER = "\t.text\n\n\t.global main\n";
  private int lCount;
  private int msgCount;

  public CodeWriter(PrintWriter file) {
    this.file = file;
    this.data = new StringBuilder(DATA_HEADER);
    this.text = new StringBuilder(TEXT_HEADER);
    this.text_p = new StringBuilder();
    this.lCount = -1;
    this.msgCount = -1;
  }

  private String addMsg(String ascii) {
    msgCount++;
    String label = "msg_" + msgCount;
    data.append("\t" + label + ":\n");
    data.append("\t\t.word " + (ascii.length() + 1) + "\n");
    data.append("\t\t.ascii\t\"" + unEscapeString(ascii) + "\\0\"\n");
    return label;
  }

  private static String unEscapeString(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      switch (s.charAt(i)) {
        case '\t':
          sb.append("\\t");
          break;
        case '\b':
          sb.append("\\b");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\'':
          sb.append("\\\'");
          break;
        case '\"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        default:
          sb.append(s.charAt(i));
          break;
      }
    }
    return sb.toString();
  }

  public void addLabel(String label) {
    text.append("\t" + label + ":\n");
  }

  public void addInst(Inst inst, String args) {
    text.append("\t\t" + inst + " " + args + "\n");
  }

  public void addLtorg() {
    text.append("\t\t.ltorg\n");
  }

  public String[] getLabelLPair() {
    String[] pair = new String[2];
    for (int i = 0; i < pair.length; i++) {
      pair[i] = getLabelL();
    }
    return pair;
  }

  private String getLabelL() {
    lCount++;
    return "L" + lCount;
  }

  public String p_throw_overflow_error() {
    String label = "p_throw_overflow_error";
    addLabelP(label);

    String msg = addMsg("OverflowError: the result is too small/large to "
        + "store in a 4-byte signed-integer.\n");
    addInstP(Inst.LDR, "r0, =" + msg);
    addInstP(Inst.BL, p_throw_runtime_error());
    return label;
  }

  public String p_throw_runtime_error() {
    String label = "p_throw_runtime_error";
    addLabelP(label);

    addInstP(Inst.BL, p_print_string());
    addInstP(Inst.MOV, "r0, #-1");
    addInstP(Inst.BL, "exit");
    return label;
  }

  public String p_check_divide_by_zero() {
    String label = "p_check_divide_by_zero";
    addLabelP(label);

    addInstP(Inst.PUSH, "{lr}");
    addInstP(Inst.CMP, "r1, #0");
    String msg = addMsg("DivideByZeroError: divide or modulo by zero\n");
    addInstP(Inst.LDREQ, "r0, =" + msg);
    addInstP(Inst.BLEQ, p_throw_runtime_error());
    addInstP(Inst.POP, "{pc}");
    return label;
  }

  public String p_print_ln() {
    String label = "p_print_ln";
    addLabelP(label);

    addInstP(Inst.PUSH, "{lr}");
    String msg = addMsg("");
    addInstP(Inst.LDR, "r0, =" + msg);
    addInstP(Inst.ADD, "r0, r0, #4");
    addInstP(Inst.BL, "puts");
    addInstP(Inst.MOV, "r0, #0");
    addInstP(Inst.BL, "fflush");
    addInstP(Inst.POP, "{pc}");
    return label;
  }

  public String p_print_int() {
    String label = "p_print_int";
    addLabelP(label);

    addInstP(Inst.PUSH, "{lr}");
    addInstP(Inst.MOV, "r1, r0");
    String msg = addMsg("%d");
    addInstP(Inst.LDR, "r0, =" + msg);
    addInstP(Inst.ADD, "r0, r0, #4");
    addInstP(Inst.BL, "printf");
    addInstP(Inst.MOV, "r0, #0");
    addInstP(Inst.BL, "fflush");
    addInstP(Inst.POP, "{pc}");
    return label;
  }

  public String p_print_bool() {
    String label = "p_print_bool";
    addLabelP(label);

    addInstP(Inst.PUSH, "{lr}");
    addInstP(Inst.CMP, "r0, #0");
    String msg0 = addMsg("true");
    addInstP(Inst.LDRNE, "r0, =" + msg0);
    String msg1 = addMsg("false");
    addInstP(Inst.LDREQ, "r0, =" + msg1);
    addInstP(Inst.ADD, "r0, r0, #4");
    addInstP(Inst.BL, "printf");
    addInstP(Inst.MOV, "r0, #0");
    addInstP(Inst.BL, "fflush");
    addInstP(Inst.POP, "{pc}");
    return label;
  }

  public String p_print_string() {
    String label = "p_print_string";
    addLabelP(label);

    addInstP(Inst.PUSH, "{lr}");
    addInstP(Inst.LDR, "{r0}");
    addInstP(Inst.ADD, "r2, r0, #4");
    String msg = addMsg("%.*s");
    addInstP(Inst.LDR, "r0, =" + msg);
    addInstP(Inst.ADD, "r0, r0, #4");
    addInstP(Inst.BL, "printf");
    addInstP(Inst.MOV, "r0, #0");
    addInstP(Inst.BL, "fflush");
    addInstP(Inst.POP, "{pc}");
    return label;
  }

  private void addLabelP(String label) {
    text_p.append("\t" + label + ":\n");
  }

  private void addInstP(Inst inst, String args) {
    text_p.append("\t\t" + inst + " " + args + "\n");
  }

  public void writeToFile() {
    if (msgCount != -1) {
      file.write(data.toString());
      file.write("\n");
    }
    file.write(text.toString());
    if (text_p.length() > 0) {
      file.write(text_p.toString());
    }
  }

}
