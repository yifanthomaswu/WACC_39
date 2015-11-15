package wacc.visitor.utils;

import antlr.BasicParser.*;
import wacc.symboltable.SymbolTable;

public class Utils {

  public static Type getType(TypeContext ctx) {
    Type type;
    if (ctx.baseType() != null) {
      type = new BaseType(ctx.baseType());
    } else if (ctx.arrayType() != null) {
      type = new ArrayType(ctx.arrayType());
    } else {
      type = new PairType(ctx.pairType());
    }
    return type;
  }

  public static Type getType(IdentContext ctx, SymbolTable st) {
    String ident = ctx.getText();
    TypeContext typeContext = (TypeContext) st.lookupAll(ident);
    return getType(typeContext);
  }

  private static Type getType(ArrayElemContext ctx, SymbolTable st) {
    ArrayType identType = (ArrayType) getType(ctx.ident(), st);
    return new ArrayType(ctx, identType);
  }

  public static Type getType(ExprContext ctx, SymbolTable st) {
    if (ctx instanceof PairExprContext) {
      return new PairType((PairExprContext) ctx);
    } else if (ctx instanceof IdentExprContext) {
      return getType(((IdentExprContext) ctx).ident(), st);
    } else if (ctx instanceof ArrayElemExprContext) {
      ArrayElemContext arrayElem = ((ArrayElemExprContext) ctx).arrayElem();
      return getType(arrayElem, st);
    } else if (ctx instanceof ParensExprContext) {
      return getType(((ParensExprContext) ctx).expr(), st);
    } else {
      return new BaseType(ctx);
    }
  }

  private static Type getType(PairElemContext ctx, SymbolTable st) {
    PairType pair = (PairType) getType(ctx.expr(), st);
    if (ctx.FST() == null) {
      return pair.getElem(0);
    } else {
      return pair.getElem(1);
    }
  }

  public static Type getType(AssignLhsContext ctx, SymbolTable st) {
    if (ctx instanceof LhsIdentContext) {
      return getType(((LhsIdentContext) ctx).ident(), st);
    } else if (ctx instanceof LhsArrayElemContext) {
      ArrayElemContext arrayElem = ((LhsArrayElemContext) ctx).arrayElem();
      return getType(arrayElem, st);
    } else {
      return getType(((LhsPairElemContext) ctx).pairElem(), st);
    }
  }

  public static Type getType(AssignRhsContext ctx, SymbolTable st) {
    if (ctx instanceof RhsExprContext) {
      return getType(((RhsExprContext) ctx).expr(), st);
    } else if (ctx instanceof RhsArrayLiterContext) {
      return new ArrayType(getType(((RhsArrayLiterContext) ctx).arrayLiter()
          .expr(0), st));
    } else if (ctx instanceof RhsNewPairContext) {
      Type[] elemTypes = new Type[2];
      for (int i = 0; i < elemTypes.length; i++) {
        elemTypes[i] = getType(((RhsNewPairContext) ctx).expr(i), st);
      }
      return new PairType(elemTypes);
    } else if (ctx instanceof RhsPairElemContext) {
      return getType(((RhsPairElemContext) ctx).pairElem(), st);
    } else {
      return getType(((RhsFunctionCallContext) ctx).ident(), st);
    }
  }

  public static boolean isSameBaseType(Type type, BaseLiter baseLiter) {
    if (type instanceof BaseType) {
      return ((BaseType) type).isSameBaseType((BaseType) type, baseLiter);
    } else {
      return type.toString().equals(baseLiter.toString()); // TODO
    }
  }

}
