package wacc.visitor.utils;

import org.antlr.v4.runtime.ParserRuleContext;

import antlr.BasicParser.*;
import wacc.visitor.SemanticErrorException;

public class Utils {

  public static Type getType(TypeContext ctx) {
    Type type;
    if (ctx.baseType() != null) {
      if (ctx.baseType().BASE_TYPE().getText().equals("string")) {
        type = new ArrayType(new BaseType());
      } else {
        type = new BaseType(ctx.baseType());
      }
    } else if (ctx.arrayType() != null) {
      type = new ArrayType(ctx.arrayType());
    } else {
      type = new PairType(ctx.pairType());
    }
    return type;
  }

  public static Type getType(IdentContext ctx, SymbolTable st) {
    String ident = ctx.getText();
    ParserRuleContext context = st.lookupAllT(ident);
    if (context == null) {
      String msg = "Variable \"" + ident + "\" is not defined in this scope";
      throw new SemanticErrorException(ctx.getParent().getStart(), msg);
    } else {
      return getType((TypeContext) context);
    }
  }

  private static Type getType(ArrayElemContext ctx, SymbolTable st) {
    Type identType = getType(ctx.ident(), st);
    if (!(identType instanceof ArrayType)
        || ((ArrayType) identType).getLevel() < ctx.expr().size()) {
      String expected = "T";
      for (int i = 0; i < ctx.expr().size(); i++) {
        expected += "[]";
      }
      String msg = "Incompatible type at " + ctx.getText() + " (expected: "
          + expected + ", actual: " + identType + ")";
      throw new SemanticErrorException(ctx.getStart(), msg);
    }
    return new ArrayType(ctx, (ArrayType) identType);
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
    } else if (ctx instanceof StringExprContext) {
      return new ArrayType(new BaseType());
    } else {
      return new BaseType(ctx);
    }
  }

  private static Type getType(PairElemContext ctx, SymbolTable st) {
    PairType pair = (PairType) getType(ctx.expr(), st);
    if (ctx.FST() != null) {
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
      ArrayLiterContext context = ((RhsArrayLiterContext) ctx).arrayLiter();
      Type exprType;
      if (context.expr().size() == 0) {
        exprType = null;
      } else {
        exprType = getType(context.expr(0), st);
      }
      for (ExprContext c : context.expr()) {
        Type cType = getType(c, st);
        if (!exprType.equals(cType)) {
          String msg = "Incompatible type at \"" + c.getText() + "\" (expected: "
              + exprType + ", actual: " + cType + ")";
          throw new SemanticErrorException(c.getStart(), msg);
        }
      }
      return new ArrayType(exprType);
    } else if (ctx instanceof RhsNewPairContext) {
      Type[] elemTypes = new Type[2];
      for (int i = 0; i < elemTypes.length; i++) {
        elemTypes[i] = getType(((RhsNewPairContext) ctx).expr(i), st);
      }
      return new PairType(elemTypes);
    } else if (ctx instanceof RhsPairElemContext) {
      return getType(((RhsPairElemContext) ctx).pairElem(), st);
    } else {
      String ident = ((RhsCallContext) ctx).ident().getText();
      ParserRuleContext context = st.lookupAllF(ident);
      if (context == null) {
        String msg = "Function \"" + ident + "\" is not defined in this scope";
        throw new SemanticErrorException(ctx.getParent().getStart(), msg);
      }
      TypeContext typeContext = ((FuncContext) context).type();
      return getType(typeContext);
    }
  }

  public static boolean isSameBaseType(Type type, BaseLiter baseLiter) {
    if (type instanceof BaseType) {
      return ((BaseType) type).isSameBaseType(baseLiter);
    } else {
      return type.toString().equals(baseLiter.toString());
    }
  }

}
