package wacc.visitor.semantic_error.utils;

import antlr.BasicParser.*;

public class ArrayType extends Type {

  private final Type base;
  private final int level;

  public ArrayType(ArrayTypeContext ctx) {
    if (ctx.baseType() != null) {
      if (ctx.baseType().BASE_TYPE().getText().equals("string")) {
        base = new ArrayType(new BaseType(BaseLiter.CHAR));
      } else {
        base = new BaseType(ctx.baseType());
      }
    } else {
      base = new PairType(ctx.pairType());
    }
    this.level = ctx.OPEN_SQUARE_BR().size();
  }

  public ArrayType(ArrayElemContext ctx, ArrayType identType) {
    base = identType.base;
    level = identType.level - ctx.expr().size();
  }

  public ArrayType(Type exprType) {
    base = exprType;
    level = 1;
  }

  public Type getBase() {
    return base;
  }

  public int getLevel() {
    return level;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ArrayType) {
      if (((Type) obj).toString().equals(toString())) {
        return true;
      }
      ArrayType that = (ArrayType) obj;
      if (level != that.level) {
        return false;
      }
      if (base == null || that.base == null) {
        return true;
      }
      return base.equals(that.base);
    }
    return ((Type) obj).toString().equals(toString());
  }

  @Override
  public int hashCode() {
    return base == null ? 0 : base.hashCode() + level;
  }

  @Override
  public String toString() {
    String brackets = "";
    for (int i = 0; i < level; i++) {
      brackets += "[]";
    }
    return base == null ? "T" : base.toString() + brackets;
  }
}
