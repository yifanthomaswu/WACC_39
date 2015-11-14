package wacc.visitor.utils;

import antlr.BasicParser.ArrayTypeContext;

public class ArrayType extends Type {

  private final Type base;
  private final int level;

  public ArrayType(ArrayTypeContext ctx) {
    if (ctx.baseType() != null) {
      base = new BaseType(ctx.baseType());
    } else {
      base = new PairType(ctx.pairType());
    }
    this.level = ctx.ARRAY_SQUARE_BRS().size();
  }

  public Type getBase() {
    return base;
  }

  public int getLevel() {
    return level;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ArrayType)) {
      return false;
    }

    ArrayType that = (ArrayType) obj;
    if (this.getLevel() != that .getLevel()) {
      return false;
    }
    return base.equals(that.getBase());
  }

  @Override
  public int hashCode() {
    return base.hashCode() + level;
  }

}
