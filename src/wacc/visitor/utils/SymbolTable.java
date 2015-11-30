package wacc.visitor.utils;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.MultiMap;

import antlr.BasicParser.*;

public class SymbolTable {

  private final SymbolTable encSymTable;
  private final MultiMap<String, ParserRuleContext> dict = new MultiMap<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public void add(String name, ParserRuleContext object) {
    dict.map(name, object);
  }

  public SymbolTable getEncSymTable() {
    return encSymTable;
  }

  private ParserRuleContext lookup(String name, Class<?> context) {
    List<ParserRuleContext> entries = dict.get(name);
    if (entries == null)
      return null;
    for (ParserRuleContext entry : entries) {
      if (context.isInstance(entry))
        return entry;
    }
    return null;
  }

  public TypeContext lookupT(String name) {
    return (TypeContext) lookup(name, TypeContext.class);
  }

  public FuncContext lookupF(String name) {
    return (FuncContext) lookup(name, FuncContext.class);
  }

  public ParserRuleContext lookupAll(String name, Class<?> context) {
    SymbolTable s = this;
    do {
      ParserRuleContext object = s.lookup(name, context);
      if (object != null) {
        return object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

  public TypeContext lookupAllT(String name) {
    return (TypeContext) lookupAll(name, TypeContext.class);
  }

  public FuncContext lookupAllF(String name) {
    return (FuncContext) lookupAll(name, FuncContext.class);

  }

}
