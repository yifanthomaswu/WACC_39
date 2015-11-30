package wacc.visitor.utils;

import java.io.PrintWriter;

public class CodeWriter {

  private final PrintWriter file;
  private final StringBuilder data;
  private final StringBuilder text;
  private static final String DATA_HEADER = "\t.data\n\n";
  private static final String TEXT_HEADER = "\t.text\n\n\t.global main\n";

  public CodeWriter(PrintWriter file) {
    this.file = file;
    this.data = new StringBuilder();
    this.text = new StringBuilder(TEXT_HEADER);
  }

  public void addLable(String lable) {
    data.append("\t" + lable + ":\n");
  }

  public void addInst(Inst inst, String args) {
    data.append("\t\t" + inst + " " + args + "\n");
  }

  public void writeToFile() {
    if (data.length() > 0) {
      file.write(DATA_HEADER);
      file.write(data.toString());
      file.write("\n");
    }
    file.write(text.toString());
  }

}
