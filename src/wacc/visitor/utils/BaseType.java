package wacc.visitor.utils;

import antlr.BasicParser.BaseTypeContext;

public class BaseType extends Type {

  private final BaseTypeContext base;

  public BaseType(BaseTypeContext ctx) {
    base = ctx;
  }

  public BaseTypeContext getBase() {
    return base;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BaseType)) {
      return false;
    }
    return base.BASE_TYPE().equals(((BaseType) obj).getBase().BASE_TYPE());
  }

  @Override
  public int hashCode() {
    return base.BASE_TYPE().toString().hashCode();
  }

}
