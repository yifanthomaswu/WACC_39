package wacc.visitor.code_generator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CodeWriter {

  private final PrintWriter file;
  private final StringBuilder data;
  private final StringBuilder text;
  private final List<StringBuilder> textP = new ArrayList<>();
  private final Set<String> definedP = new HashSet<>();
  private int msgCount;
  private int lCount;

  public CodeWriter(PrintWriter file) {
    this.file = file;
    this.data = new StringBuilder("\t.data\n\n");
    this.text = new StringBuilder("\t.text\n\n\t.global main\n");
    this.msgCount = -1;
    this.lCount = -1;
  }

  public String addMsg(String ascii) {
    msgCount++;
    String label = "msg_" + msgCount;
    addLabelToSB(label, data);
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

  private void addLabelToSB(String label, StringBuilder sb) {
    sb.append("\t" + label + ":\n");
  }

  private void addInstToSB(Inst inst, String args, StringBuilder sb) {
    sb.append("\t\t" + inst + " " + args + "\n");
  }

  public void addLabel(String label) {
    addLabelToSB(label, text);
  }

  public void addInst(Inst inst, String args) {
    addInstToSB(inst, args, text);
  }

  public void addLtorg() {
    text.append("\t\t.ltorg\n");
  }

  public String[] getLabelLPair() {
    String[] lpair = new String[2];
    for (int i = 0; i < lpair.length; i++) {
      lpair[i] = getLabelL();
    }
    return lpair;
  }

  private String getLabelL() {
    lCount++;
    return "L" + lCount;
  }

  private StringBuilder initP(String label) {
    definedP.add(label);
    StringBuilder sb = new StringBuilder();
    textP.add(sb);
    addLabelToSB(label, sb);
    return sb;
  }

  public String p_throw_runtime_error() {
    String label = "p_throw_runtime_error";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.BL, p_print_string(), sb);
    addInstToSB(Inst.MOV, "r0, #-1", sb);
    addInstToSB(Inst.BL, "exit", sb);
    return label;
  }

  public String p_throw_overflow_error() {
    String label = "p_throw_overflow_error";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    String msg = addMsg("OverflowError: the result is too small/large to "
        + "store in a 4-byte signed-integer.\\n\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.BL, p_throw_runtime_error(), sb);
    return label;
  }

  public String p_check_divide_by_zero() {
    String label = "p_check_divide_by_zero";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.CMP, "r1, #0", sb);
    String msg = addMsg("DivideByZeroError: divide or modulo by zero\\n\\0");
    addInstToSB(Inst.LDREQ, "r0, =" + msg, sb);
    addInstToSB(Inst.BLEQ, p_throw_runtime_error(), sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_check_null_pointer() {
    String label = "p_check_null_pointer";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.CMP, "r0, #0", sb);
    String msg = addMsg("NullReferenceError: dereference a null reference\\n\\0");
    addInstToSB(Inst.LDREQ, "r0, =" + msg, sb);
    addInstToSB(Inst.BLEQ, p_throw_runtime_error(), sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_check_array_bounds() {
    String label = "p_check_array_bounds";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.CMP, "r0, #0", sb);
    String msg0 = addMsg("ArrayIndexOutOfBoundsError: negative index\\n\\0");
    addInstToSB(Inst.LDRLT, "r0, =" + msg0, sb);
    addInstToSB(Inst.BLLT, p_throw_runtime_error(), sb);
    addInstToSB(Inst.LDR, "r1, [r1]", sb);
    addInstToSB(Inst.CMP, "r0, r1", sb);
    String msg1 = addMsg("ArrayIndexOutOfBoundsError: index too large\\n\\0");
    addInstToSB(Inst.LDRCS, "r0, =" + msg1, sb);
    addInstToSB(Inst.BLCS, p_throw_runtime_error(), sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_ln() {
    String label = "p_print_ln";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    String msg = addMsg("\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "puts", sb);
    addInstToSB(Inst.MOV, "r0, #0", sb);
    addInstToSB(Inst.BL, "fflush", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_int() {
    String label = "p_print_int";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.MOV, "r1, r0", sb);
    String msg = addMsg("%d\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "printf", sb);
    addInstToSB(Inst.MOV, "r0, #0", sb);
    addInstToSB(Inst.BL, "fflush", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_bool() {
    String label = "p_print_bool";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.CMP, "r0, #0", sb);
    String msg0 = addMsg("true\\0");
    addInstToSB(Inst.LDRNE, "r0, =" + msg0, sb);
    String msg1 = addMsg("false\\0");
    addInstToSB(Inst.LDREQ, "r0, =" + msg1, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "printf", sb);
    addInstToSB(Inst.MOV, "r0, #0", sb);
    addInstToSB(Inst.BL, "fflush", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_string() {
    String label = "p_print_string";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.LDR, "r1, [r0]", sb);
    addInstToSB(Inst.ADD, "r2, r0, #4", sb);
    String msg = addMsg("%.*s\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "printf", sb);
    addInstToSB(Inst.MOV, "r0, #0", sb);
    addInstToSB(Inst.BL, "fflush", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_print_reference() {
    String label = "p_print_reference";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.MOV, "r1, r0", sb);
    String msg = addMsg("%p\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "printf", sb);
    addInstToSB(Inst.MOV, "r0, #0", sb);
    addInstToSB(Inst.BL, "fflush", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_read_int() {
    String label = "p_read_int";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.MOV, "r1, r0", sb);
    String msg = addMsg("%d\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "scanf", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_read_char() {
    String label = "p_read_char";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.MOV, "r1, r0", sb);
    String msg = addMsg(" %c\\0");
    addInstToSB(Inst.LDR, "r0, =" + msg, sb);
    addInstToSB(Inst.ADD, "r0, r0, #4", sb);
    addInstToSB(Inst.BL, "scanf", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_free_pair() {
    String label = "p_free_pair";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);
    addInstToSB(Inst.MOV, "r0, #134", sb);
    addInstToSB(Inst.BL, "exit", sb);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.CMP, "r0, #0", sb);
    String msg = addMsg("NullReferenceError: dereference a null reference\\n\\0");
    addInstToSB(Inst.LDREQ, "r0, =" + msg, sb);
    addInstToSB(Inst.BEQ, p_throw_runtime_error(), sb);
    addInstToSB(Inst.PUSH, "{r0}", sb);
    addInstToSB(Inst.LDR, "r0, [r0]", sb);
    addInstToSB(Inst.BL, "free", sb);
    addInstToSB(Inst.CMP, "r0, #66", sb);
    addInstToSB(Inst.MOVEQ, "r0, #134", sb);
    addInstToSB(Inst.BLEQ, "exit", sb);
    addInstToSB(Inst.LDR, "r0, [sp]", sb);
    addInstToSB(Inst.LDR, "r0, [r0, #4]", sb);
    addInstToSB(Inst.BL, "free", sb);
    addInstToSB(Inst.POP, "{r0}", sb);
    addInstToSB(Inst.BL, "free", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public String p_free_array() {
    String label = "p_free_array";
    if (definedP.contains(label)) {
      return label;
    }
    StringBuilder sb = initP(label);

    addInstToSB(Inst.PUSH, "{lr}", sb);
    addInstToSB(Inst.CMP, "r0, #0", sb);
    String msg = addMsg("NullReferenceError: dereference a null reference\\n\\0");
    addInstToSB(Inst.LDREQ, "r0, =" + msg, sb);
    addInstToSB(Inst.BEQ, p_throw_runtime_error(), sb);
    addInstToSB(Inst.BL, "free", sb);
    addInstToSB(Inst.CMP, "r0, #78", sb);
    addInstToSB(Inst.MOVEQ, "r0, #134", sb);
    addInstToSB(Inst.BLEQ, "exit", sb);
    addInstToSB(Inst.POP, "{pc}", sb);
    return label;
  }

  public void writeToFile() {
    if (msgCount != -1) {
      file.write(data.toString());
      file.write("\n");
    }
    file.write(text.toString());
    for (StringBuilder sb : textP) {
      file.write(sb.toString());
    }
  }

}
