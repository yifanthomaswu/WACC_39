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

  public TypeContext lookupT(String name) {
    List<ParserRuleContext> entries = dict.get(name);
    if (entries == null)
      return null;
    for (ParserRuleContext entry : entries) {
      if (entry instanceof TypeContext)
        return (TypeContext) entry;
    }
    return null;
  }

  public FuncContext lookupF(String name) {
    List<ParserRuleContext> entries = dict.get(name);
    if (entries == null)
      return null;
    for (ParserRuleContext entry : entries) {
      if (entry instanceof FuncContext)
        return (FuncContext) entry;
    }
    return null;
  }

  public TypeContext lookupAllT(String name) {
    SymbolTable s = this;
    do {
      ParserRuleContext object = s.lookupT(name);
      if (object != null) {
        return (TypeContext) object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

  public FuncContext lookupAllF(String name) {
    SymbolTable s = this;
    do {
      ParserRuleContext object = s.lookupF(name);
      if (object != null) {
        return (FuncContext) object;
      }
      s = s.encSymTable;
    } while (s != null);
    return null;
  }

}
