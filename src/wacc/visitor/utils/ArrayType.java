package wacc.visitor.utils;

import antlr.BasicParser.*;

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

  public ArrayType(ArrayElemContext ctx, ArrayType identType) {
    base = identType.base;
    level = identType.level - ctx.expr().size();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ArrayType)) {
      return false;
    }

    ArrayType that = (ArrayType) obj;
    if (level != that.level) {
      return false;
    }
    return base.equals(that.base);
  }

  @Override
  public int hashCode() {
    return base.hashCode() + level;
  }

  public int getLevel() {
    return level;
  }
  
  public Type getBaseType() {
    return base;
  }
}
