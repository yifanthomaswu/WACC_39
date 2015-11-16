package wacc.symboltable;

import java.util.List;

import antlr.BasicParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.MultiMap;


public class SymbolTable {

  private final SymbolTable encSymTable;
  private final MultiMap<String, ParserRuleContext> dict = new MultiMap<>();
 // private final Dictionary<String, ParserRuleContext> dict = new Hashtable<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public void add(String name, ParserRuleContext object) {
    dict.map(name, object);
  }

  public SymbolTable getEncSymTable() {
    return encSymTable;
  }

  /*
  public ParserRuleContext lookup(String name) {
    if (dict.get(name) != null)
      return dict.get(name).get(0);
    else
      return null;
   // return dict.get(name);
  }
  */

  public ParserRuleContext lookupT(String name) {
    List<ParserRuleContext> entries = dict.get(name);
    for (ParserRuleContext entry : entries) {
      if (entry instanceof BasicParser.TypeContext)
        return entry;
    }
    return null;
  }

  public ParserRuleContext lookupF(String name) {
    List<ParserRuleContext> entries = dict.get(name);
    for (ParserRuleContext entry : entries) {
      if (entry instanceof BasicParser.FuncContext)
        return entry;
    }
    return null;
  }

  public ParserRuleContext lookupAllF(String name) {
    SymbolTable s = this;
    do {
      ParserRuleContext object = s.lookupF(name);
      if (object != null) {
        return object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

  public ParserRuleContext lookupAllT(String name) {
    SymbolTable s = this;
    do {
      ParserRuleContext object = s.lookupT(name);
      if (object != null) {
        return object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

}
