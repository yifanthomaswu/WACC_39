package wacc.visitor.code_generator;

import java.util.Dictionary;
import java.util.Hashtable;

public class SymbolTable {

  private SymbolTable encSymTable;
  private final Dictionary<String, Integer> dict = new Hashtable<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public SymbolTable getEncSymTable() {
    return encSymTable;
  }

  public void add(String name, Integer obj) {
    dict.put(name, obj);
  }

  public Integer lookup(String name) {
    return dict.get(name);
  }

  public Integer lookupAll(String name) {
    SymbolTable s = this;
    do {
      Integer obj = s.lookup(name);
      if (obj != null) {
        return obj;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

}
