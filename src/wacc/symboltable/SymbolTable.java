package wacc.symboltable;

import java.util.Dictionary;
import java.util.Hashtable;

import org.antlr.v4.runtime.ParserRuleContext;

public class SymbolTable {

  private final SymbolTable encSymTable;
  private final Dictionary<String, ParserRuleContext> dict = new Hashtable<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public void add(String name, ParserRuleContext object) {
    dict.put(name, object);
  }

  public ParserRuleContext lookup(String name) {
    return dict.get(name);
  }

  public ParserRuleContext lookupAll(String name) {
    SymbolTable s = this;
    do {
      ParserRuleContext object = s.lookup(name);
      if (object != null) {
        return object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

}
