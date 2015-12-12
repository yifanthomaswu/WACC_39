package wacc.visitor.type;

import java.util.List;

import antlr.WACCParser.*;
import wacc.visitor.SymbolTable;
import wacc.visitor.semantic_error.SemanticErrorException;

public class Utils {

  public static Type getType(TypeContext ctx) {
    Type type;
    if (ctx.baseType() != null) {
      if (ctx.baseType().BASE_TYPE().getText().equals("string")) {
        type = new ArrayType(new BaseType(BaseLiter.CHAR));
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
    TypeContext context = st.lookupAllT(ident);
    if (context == null) {
      String msg = "Variable \"" + ident + "\" is not defined in this scope";
      throw new SemanticErrorException(ctx.getParent().getStart(), msg);
    } else {
      return getType(context);
    }
  }

  public static Type getType(ArrayElemContext ctx, SymbolTable st) {
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
      return new ArrayType(new BaseType(BaseLiter.CHAR));
    } else {
      return new BaseType(ctx);
    }
  }

  public static Type getType(PairElemContext ctx, SymbolTable st) {
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
          String msg = "Incompatible type at \"" + c.getText()
              + "\" (expected: " + exprType + ", actual: " + cType + ")";
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
      RhsCallContext context = (RhsCallContext) ctx;
      if (!Utils.isCallable(context, st)) {
        String signature = context.ident().getText() + "(";
        if (Utils.getArgSize(context) != 0) {
          for (ExprContext c : context.argList().expr()) {
            signature += Utils.getType(c, st).toString() + ",";
          }
          signature = signature.substring(0, signature.length() - 1);
        }
        signature += ")";
        String msg = "\"" + signature + "\" is not defined in this scope";
        throw new SemanticErrorException(context.getStart(), msg);
      }
      return getType(getFuncWithSameSignature(context, st).type());
    }
  }

  public static boolean isSameBaseType(Type type, BaseLiter baseLiter) {
    if (type instanceof BaseType) {
      return ((BaseType) type).isSameBaseType(baseLiter);
    } else {
      return type.toString().equals(baseLiter.toString());
    }
  }

  public static boolean isStringType(Type type) {
    return type.toString().equals("CHAR[]");
  }

  public static boolean isDefinable(FuncContext ctx, SymbolTable st) {
    List<FuncContext> funcs = st.lookupAllF(ctx.ident().getText());
    if (funcs.size() == 0) {
      return true;
    }
    for (FuncContext func : funcs) {
      if (isSameSignature(ctx, func)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isCallable(RhsCallContext ctx, SymbolTable st) {
    List<FuncContext> funcs = st.lookupAllF(ctx.ident().getText());
    if (funcs.size() == 0) {
      return false;
    }
    for (FuncContext func : funcs) {
      if (isSameSignature(ctx, func, st)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isSameSignature(FuncContext ctx, FuncContext func) {
    boolean isSame = true;
    int paramSize = getParamSize(ctx);
    if (paramSize == getParamSize(func)) {
      for (int i = 0; i < paramSize; i++) {
        Type paramType = getType(ctx.paramList().param(i).type());
        if (!paramType.equals(getType(func.paramList().param(i).type()))) {
          isSame = false;
          break;
        }
      }
    } else {
      isSame = false;
    }
    return isSame;
  }

  public static boolean isSameSignature(RhsCallContext ctx, FuncContext func,
      SymbolTable st) {
    boolean isSame = true;
    int argSize = getArgSize(ctx);
    if (argSize == getParamSize(func)) {
      for (int i = 0; i < argSize; i++) {
        Type argType = getType(ctx.argList().expr(i), st);
        if (!argType.equals(getType(func.paramList().param(i).type()))) {
          isSame = false;
          break;
        }
      }
    } else {
      isSame = false;
    }
    return isSame;
  }

  public static FuncContext getFuncWithSameSignature(RhsCallContext ctx,
      SymbolTable st) {
    List<FuncContext> funcs = st.lookupAllF(ctx.ident().getText());
    if (funcs.size() == 0) {
      return null;
    }
    for (FuncContext func : funcs) {
      if (isSameSignature(ctx, func, st)) {
        return func;
      }
    }
    return null;
  }

  public static int getParamSize(FuncContext ctx) {
    int paramSize = 0;
    if (ctx.paramList() != null) {
      paramSize = ctx.paramList().param().size();
    }
    return paramSize;
  }

  public static int getArgSize(RhsCallContext ctx) {
    int argSize = 0;
    if (ctx.argList() != null) {
      argSize = ctx.argList().expr().size();
    }
    return argSize;
  }

}
