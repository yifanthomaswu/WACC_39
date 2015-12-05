package wacc.visitor;

import java.util.List;

import org.antlr.v4.runtime.misc.MultiMap;

import antlr.BasicParser.FuncContext;
import antlr.BasicParser.TypeContext;

public class SymbolTable {

  private final SymbolTable encSymTable;
  private final MultiMap<String, Object> dict = new MultiMap<>();

  public SymbolTable(SymbolTable st) {
    encSymTable = st;
  }

  public SymbolTable getEncSymTable() {
    return encSymTable;
  }

  public void add(String name, Object object) {
    dict.map(name, object);
  }

  private Object lookup(String name, Class<?> c) {
    List<Object> objects = dict.get(name);
    if (objects == null) {
      return null;
    }
    for (Object object : objects) {
      if (c.isInstance(object)) {
        return object;
      }
    }
    return null;
  }

  public TypeContext lookupT(String name) {
    return (TypeContext) lookup(name, TypeContext.class);
  }

  public FuncContext lookupF(String name) {
    return (FuncContext) lookup(name, FuncContext.class);
  }

  public Integer lookupI(String name) {
    return (Integer) lookup(name, Integer.class);
  }

  public Object lookupAll(String name, Class<?> c) {
    SymbolTable s = this;
    do {
      Object object = s.lookup(name, c);
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

  public Integer lookupAllI(String name) {
    return (Integer) lookupAll(name, Integer.class);
  }

}
