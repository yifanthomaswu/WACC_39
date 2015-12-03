package wacc.visitor.code_generator;

import java.io.PrintWriter;

public class CodeWriter {

  private final PrintWriter file;
  private final StringBuilder data;
  private final StringBuilder text;
  private static final String DATA_HEADER = "\t.data\n\n";
  private static final String TEXT_HEADER = "\t.text\n\n\t.global main\n";
  private int labelL;

  public CodeWriter(PrintWriter file) {
    this.file = file;
    this.data = new StringBuilder(DATA_HEADER);
    this.text = new StringBuilder(TEXT_HEADER);
    this.labelL = -1;
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
    labelL++;
    return "L" + labelL;
  }

  public void writeToFile() {
    if (data.length() > DATA_HEADER.length()) {
      file.write(data.toString());
      file.write("\n");
    }
    file.write(text.toString());
  }

}
