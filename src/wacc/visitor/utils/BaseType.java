package wacc.visitor.utils;

import antlr.BasicParser.*;

public class BaseType extends Type {

  private enum Base {
    INT, BOOL, CHAR, STRING;
  }

  private final Base base;
  private static final String BINARY_OPER_RETURN_INT = "*/%+-";

  public BaseType(BaseTypeContext ctx) {
    base = Base.valueOf(ctx.BASE_TYPE().toString().toUpperCase());
  }

  public BaseType(ExprContext ctx) {
    if (ctx instanceof IntExprContext) {
      base = Base.INT;
    } else if (ctx instanceof BoolExprContext) {
      base = Base.BOOL;
    } else if (ctx instanceof CharExprContext) {
      base = Base.CHAR;
    } else if (ctx instanceof StringExprContext) {
      base = Base.STRING;
    } else if (ctx instanceof UnOpExprContext) {
      UnaryOperContext unaryOper = ((UnOpExprContext) ctx).unaryOper();
      if (unaryOper.MINUS() != null) {
        base = Base.INT;
      } else {
        String oper = unaryOper.UNARY_OPER().toString();
        if (oper.equals("!")) {
          base = Base.BOOL;
        } else if (oper.equals("chr")) {
          base = Base.CHAR;
        } else {
          base = Base.INT;
        }
      }
    } else {
      BinaryOperContext binaryOper = ((BinOpExprContext) ctx).binaryOper();
      if (binaryOper.MINUS() != null) {
        base = Base.INT;
      } else {
        String oper = binaryOper.BINARY_OPER().toString();
        if (BINARY_OPER_RETURN_INT.contains(oper)) {
          base = Base.INT;
        } else {
          base = Base.BOOL;
        }
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof BaseType)) {
      return false;
    }
    return base == ((BaseType) obj).base;
  }

  @Override
  public int hashCode() {
    return base.hashCode();
  }

  @Override
  public String toString() {
    return base.toString();
  }

}
