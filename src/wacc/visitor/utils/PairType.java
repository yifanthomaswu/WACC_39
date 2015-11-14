package wacc.visitor.utils;

import antlr.BasicParser.*;

public class PairType extends Type {

  private final Type[] base = new Type[2];

  public PairType(PairTypeContext ctx) {
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

  public Type getBase(int i) {
    return base[i];
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PairType)) {
      return false;
    }

    PairType that = (PairType) obj;
    for (int i = 0; i < base.length; i++) {
      if (!(base[i] == null && that.getBase(i) == null)) {
        if (base[i] != null && that.getBase(i) != null) {
          if (!base[i].equals(that.getBase(i))) {
            return false;
          }
        } else {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return ((base[1] == null) ? 0 : base[1].hashCode()) +
        ((base[2] == null) ? 0 : base[2].hashCode());
  }

}
