package wacc.visitor.code_generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CodeWriter {

  private final PrintWriter file;
  private final StringBuilder data;
  private final StringBuilder text;
  private final List<StringBuilder> text_p = new ArrayList<>();
  private static final String DATA_HEADER = "\t.data\n\n";
  private static final String TEXT_HEADER = "\t.text\n\n\t.global main\n";
  private int lCount;
  private int msgCount;

  private boolean p_throw_overflow_error;
  private boolean p_throw_runtime_error;
  private boolean p_check_divide_by_zero;
  private boolean p_print_reference;
  private boolean p_check_null_pointer;
  private boolean p_print_ln;
  private boolean p_print_int;
  private boolean p_print_bool;
  private boolean p_print_string;

  public CodeWriter(PrintWriter file) {
    this.file = file;
    this.data = new StringBuilder(DATA_HEADER);
    this.text = new StringBuilder(TEXT_HEADER);
    this.lCount = -1;
    this.msgCount = -1;
    this.p_throw_overflow_error = false;
    this.p_throw_runtime_error = false;
    this.p_check_divide_by_zero = false;
    this.p_print_ln = false;
    this.p_print_int = false;
    this.p_print_bool = false;
    this.p_print_string = false;
  }

  public String addMsg(String ascii) {
    msgCount++;
    String label = "msg_" + msgCount;
    addLabel(label, data);
    data.append("\t\t.word " + wordInAscii(ascii) + "\n");
    data.append("\t\t.ascii\t\"" + ascii + "\"\n");
    return label;
  }

  private static int wordInAscii(String s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      count++;
      if (s.charAt(i) == '\\') {
        i++;
      }
    }
    return count;
  }

  private void addLabel(String label, StringBuilder sb) {
    sb.append("\t" + label + ":\n");
  }

  private void addInst(Inst inst, String args, StringBuilder sb) {
    sb.append("\t\t" + inst + " " + args + "\n");
  }

  public void addLabel(String label) {
    addLabel(label, text);
  }

  public void addInst(Inst inst, String args) {
    addInst(inst, args, text);
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
    if (p_throw_overflow_error) {
      return label;
    }
    p_throw_overflow_error = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    String msg = addMsg("OverflowError: the result is too small/large to "
        + "store in a 4-byte signed-integer.\\n\\0");
    addInst(Inst.LDR, "r0, =" + msg, sb);
    addInst(Inst.BL, p_throw_runtime_error(), sb);
    return label;
  }

  public String p_throw_runtime_error() {
    String label = "p_throw_runtime_error";
    if (p_throw_runtime_error) {
      return label;
    }
    p_throw_runtime_error = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.BL, p_print_string(), sb);
    addInst(Inst.MOV, "r0, #-1", sb);
    addInst(Inst.BL, "exit", sb);
    return label;
  }

  public String p_check_divide_by_zero() {
    String label = "p_check_divide_by_zero";
    if (p_check_divide_by_zero) {
      return label;
    }
    p_check_divide_by_zero = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    addInst(Inst.CMP, "r1, #0", sb);
    String msg = addMsg("DivideByZeroError: divide or modulo by zero\\n\\0");
    addInst(Inst.LDREQ, "r0, =" + msg, sb);
    addInst(Inst.BLEQ, p_throw_runtime_error(), sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_reference() {
    String label = "p_print_reference";
    if (p_print_reference) {
      return label;
    }
    p_print_reference = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    addInst(Inst.MOV, "r1, r0", sb);
    String msg = addMsg("%p\\0");
    addInst(Inst.LDR, "r0, =" + msg, sb);
    addInst(Inst.ADD, "r0, r0, #4", sb);
    addInst(Inst.BL, "fflush", sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_check_null_pointer() {
    String label = "p_check_null_pointer";
    if (p_check_null_pointer) {
      return label;
    }
    p_check_null_pointer = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    addInst(Inst.CMP, "r0, #0", sb);
    String msg = addMsg("NullReferenceError: dereference a null reference\\n\\0");
    addInst(Inst.LDREQ, "r0, =" + msg, sb);
    addInst(Inst.BLEQ, p_throw_runtime_error(), sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_ln() {
    String label = "p_print_ln";
    if (p_print_ln) {
      return label;
    }
    p_print_ln = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    String msg = addMsg("\\0");
    addInst(Inst.LDR, "r0, =" + msg, sb);
    addInst(Inst.ADD, "r0, r0, #4", sb);
    addInst(Inst.BL, "puts", sb);
    addInst(Inst.MOV, "r0, #0", sb);
    addInst(Inst.BL, "fflush", sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_int() {
    String label = "p_print_int";
    if (p_print_int) {
      return label;
    }
    p_print_int = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    addInst(Inst.MOV, "r1, r0", sb);
    String msg = addMsg("%d\\0");
    addInst(Inst.LDR, "r0, =" + msg, sb);
    addInst(Inst.ADD, "r0, r0, #4", sb);
    addInst(Inst.BL, "printf", sb);
    addInst(Inst.MOV, "r0, #0", sb);
    addInst(Inst.BL, "fflush", sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_bool() {
    String label = "p_print_bool";
    if (p_print_bool) {
      return label;
    }
    p_print_bool = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    addInst(Inst.CMP, "r0, #0", sb);
    String msg0 = addMsg("true\\0");
    addInst(Inst.LDRNE, "r0, =" + msg0, sb);
    String msg1 = addMsg("false\\0");
    addInst(Inst.LDREQ, "r0, =" + msg1, sb);
    addInst(Inst.ADD, "r0, r0, #4", sb);
    addInst(Inst.BL, "printf", sb);
    addInst(Inst.MOV, "r0, #0", sb);
    addInst(Inst.BL, "fflush", sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_string() {
    String label = "p_print_string";
    if (p_print_string) {
      return label;
    }
    p_print_string = true;
    StringBuilder sb = new StringBuilder();
    text_p.add(sb);
    addLabel(label, sb);

    addInst(Inst.PUSH, "{lr}", sb);
    addInst(Inst.LDR, "r1, [r0]", sb);
    addInst(Inst.ADD, "r2, r0, #4", sb);
    String msg = addMsg("%.*s\\0");
    addInst(Inst.LDR, "r0, =" + msg, sb);
    addInst(Inst.ADD, "r0, r0, #4", sb);
    addInst(Inst.BL, "printf", sb);
    addInst(Inst.MOV, "r0, #0", sb);
    addInst(Inst.BL, "fflush", sb);
    addInst(Inst.POP, "{pc}", sb);
    return label;
  }

  public void writeToFile() {
    if (msgCount != -1) {
      file.write(data.toString());
      file.write("\n");
    }
    file.write(text.toString());
    for (StringBuilder sb : text_p) {
      file.write(sb.toString());
    }
  }

}
