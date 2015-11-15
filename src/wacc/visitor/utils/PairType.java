package wacc.visitor.utils;

import antlr.BasicParser.*;

public class PairType extends Type {

  private final Type[] base;

  public PairType(PairTypeContext ctx) {
    base = new Type[2];
    for (int i = 0; i < base.length; i++) {
      PairElemTypeContext pairElemType = ctx.pairElemType(i);
      if (pairElemType.baseType() != null) {
        base[i] = new BaseType(pairElemType.baseType());
      } else if (pairElemType.arrayType() != null) {
        base[i] = new ArrayType(pairElemType.arrayType());
      } else {
        base[i] = null;
      }
    }
  }

  public PairType(PairExprContext ctx) {
    base = null;
  }

  public PairType(Type[] elemTypes) {
    base = new Type[2];
    for (int i = 0; i < base.length; i++) {
      base[i] = elemTypes[i];
    }
  }

  Type getElem(int i) {
    return base[i];
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PairType)) {
      return false;
    }

    Type[] thatBase = ((PairType) obj).base;
    if (!(base == null && thatBase == null)) {
      if (base != null && thatBase != null) {
        for (int i = 0; i < base.length; i++) {
          if (!(base[i] == null && thatBase[i] == null)) {
            if (base[i] != null && thatBase[i] != null) {
              if (!base[i].equals(thatBase[i])) {
                return false;
              }
            } else {
              return false;
            }
          }
        }
      } else {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    if (base == null) {
      return 0;
    } else {
      return (base[1] == null ? 0 : base[1].hashCode())
          + (base[2] == null ? 0 : base[2].hashCode());
    }
  }

  @Override
  public String toString() {
    if (base == null) {
      return "NULL";
    } else {
      return "PAIR(" + (base[0] == null ? "PAIR" : base[0].toString()) + "," +
          (base[1] == null ? "PAIR" : base[1].toString()) + ")";
    }
  }
}
