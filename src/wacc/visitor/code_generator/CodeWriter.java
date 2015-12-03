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

  public String checkDivideByZero() {
    String label = "p_check_divide_by_zero";
    addLabelP(label);
    addInstP(Inst.PUSH, "{lr}");
    addInstP(Inst.CMP, "r1, #0");
    String msg = addMsg("DivideByZeroError: divide or modulo by zero\n");
    addInstP(Inst.LDREQ, "=" + msg);
    addInstP(Inst.BLEQ, throwRuntimeError());
    addInstP(Inst.POP, "{pc}");
    return label;
  }

  private String throwRuntimeError() {
    String label = "p_throw_runtime_error";
    addLabelP(label);
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
