package wacc.visitor.semantic_error.utils;

import antlr.BasicParser.*;

public class BaseType extends Type {

  private final BaseLiter baseLiter;

  public BaseType(BaseTypeContext ctx) {
    baseLiter = BaseLiter.valueOf(ctx.BASE_TYPE().toString().toUpperCase());
  }

  public BaseType(ExprContext ctx) {
    if (ctx instanceof IntExprContext) {
      baseLiter = BaseLiter.INT;
    } else if (ctx instanceof BoolExprContext) {
      baseLiter = BaseLiter.BOOL;
    } else if (ctx instanceof CharExprContext) {
      baseLiter = BaseLiter.CHAR;
    } else if (ctx instanceof UnOpExprContext) {
      UnaryOperContext unaryOper = ((UnOpExprContext) ctx).unaryOper();
      if (unaryOper.MINUS() != null) {
        baseLiter = BaseLiter.INT;
      } else {
        String oper = unaryOper.UNARY_OPER().toString();
        if (oper.equals("!")) {
          baseLiter = BaseLiter.BOOL;
        } else if (oper.equals("chr")) {
          baseLiter = BaseLiter.CHAR;
        } else { // in the cases of 'len' and 'ord'
          baseLiter = BaseLiter.INT;
        }
      }
    } else {
      if (ctx instanceof BinOpPrec1ExprContext
          || ctx instanceof BinOpPrec2ExprContext) {
        baseLiter = BaseLiter.INT;
      } else {
        baseLiter = BaseLiter.BOOL;
      }
    }
  }

  BaseType(BaseLiter baseLiter) {
    this.baseLiter = baseLiter;
  }

  boolean isSameBaseType(BaseLiter baseLiter) {
    return this.baseLiter == baseLiter;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BaseType) {
      return baseLiter.equals(((BaseType) obj).baseLiter);
    }
    return ((Type) obj).toString().equals(toString());
  }

  @Override
  public int hashCode() {
    return baseLiter.hashCode();
  }

  @Override
  public String toString() {
    return baseLiter.toString();
  }

}
