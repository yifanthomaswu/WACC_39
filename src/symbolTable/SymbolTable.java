package symbolTable;

import java.util.Dictionary;
import java.util.Hashtable;

public class SymbolTable {

  private SymbolTable encSymTable;
  private final Dictionary<String, Identifier> dict = new Hashtable<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public void add(String name, Identifier object) {
    dict.put(name, object);
  }

  public Identifier lookup(String name) {
    return dict.get(name);
  }

  public Identifier lookupAll(String name) {
    SymbolTable s = this;
    do {
      Identifier object = s.lookup(name);
      if (object != null) {
        return object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }
}