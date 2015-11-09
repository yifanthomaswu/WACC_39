import java.util.*;
import antlr.*;

public class MyVisitor extends BasicParserBaseVisitor<Void> {

  public Void visitProgram(BasicParser.ProgramContext ctx) {
    return visitChildren(ctx);
  }

  public Void visitIntLiter(BasicParser.IntLiterContext ctx) {
//    System.out.println("yoyoy");
    long l = Long.parseLong(ctx.INT_LITER().getSymbol().getText());
    if (l > Integer.MAX_VALUE)
    {
      System.out.println("Integer value " + l + " on line " + ctx.INT_LITER().getSymbol().getLine() + " is too large for a 32-bit signed integer\n" +
              "Parsing aborted, no further compilation attempted.");
      System.exit(100);
    }
    return visitChildren(ctx); }

}

