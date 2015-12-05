package wacc.visitor.type;

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
      if (elemTypes[i] instanceof PairType) {
        base[i] = null;
      } else {
        base[i] = elemTypes[i];
      }
    }
  }

  private PairType() {
    base = null;
  }

  Type getElem(int i) {
    if (base[i] == null) {
      return new PairType();
    } else {
      return base[i];
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PairType) {
      Type[] thatBase = ((PairType) obj).base;
      if (base == null || thatBase == null) {
        return true;
      }
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
    return ((Type) obj).toString().equals(toString());
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
      return "PAIR(" + (base[0] == null ? "PAIR" : base[0].toString()) + ","
          + (base[1] == null ? "PAIR" : base[1].toString()) + ")";
    }
  }
}
