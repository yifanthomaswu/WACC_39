package wacc.symboltable;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import antlr.BasicParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.MultiMap;
import wacc.visitor.utils.Utils;

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

  public ParserRuleContext lookup(String name) {
    if (dict.get(name) != null)
      return dict.get(name).get(0);
    else
      return null;
   // return dict.get(name);
  }

  public ParserRuleContext lookup(String name, BasicParser.TypeContext type) {
    List<ParserRuleContext> entries = dict.get(name);
    for (ParserRuleContext entry : entries) {
      if (entry instanceof BasicParser.TypeContext)
        if (Utils.getType((BasicParser.TypeContext)entry).equals(Utils.getType(type))) {
        return entry;
      }
    }
    return null;
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
