package wacc.visitor.code_generator;

import java.io.PrintWriter;

public class CodeWriter {

  private final PrintWriter file;
  private final StringBuilder data;
  private final StringBuilder text;
  private static final String DATA_HEADER = "\t.data\n\n";
  private static final String TEXT_HEADER = "\t.text\n\n\t.global main\n";

  public CodeWriter(PrintWriter file) {
    this.file = file;
    this.data = new StringBuilder(DATA_HEADER);
    this.text = new StringBuilder(TEXT_HEADER);
  }

  public void addLable(String lable) {
    text.append("\t" + lable + ":\n");
  }

  public void addInst(Inst inst, String args) {
    text.append("\t\t" + inst + " " + args + "\n");
  }

  public void addLtorg() {
    text.append("\t\t.ltorg\n");
  }

  public void writeToFile() {
    if (data.length() > DATA_HEADER.length()) {
      file.write(data.toString());
      file.write("\n");
    }
    file.write(text.toString());
  }

}
