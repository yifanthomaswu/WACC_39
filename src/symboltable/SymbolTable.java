package symboltable;

import java.util.Dictionary;
import java.util.Hashtable;

public class SymbolTable {

  private SymbolTable encSymTable;
  private final Dictionary<String, String> dict = new Hashtable<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public void add(String name, String obj) {
    dict.put(name, obj);
  }

  public String lookup(String name) {
    return dict.get(name);
  }

  public String lookupAll(String name) {
    SymbolTable s = this;
    do {
      String obj = s.lookup(name);
      if (obj != null) {
        return obj;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }
}
