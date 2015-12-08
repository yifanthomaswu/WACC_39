package wacc.visitor.syntactic_error;

import antlr.*;
import antlr.WACCParser.*;

public class SyntacticVisitor extends WACCParserBaseVisitor<Void> {

  @Override
  public Void visitFunc(FuncContext ctx) {
    if (!hasReturn(ctx.stat())) {
      String msg = "Function " + ctx.ident().getText()
          + " is not ended with a return or an exit statement.";
      throw new SyntacticErrorException(ctx.getStart(), msg);
    }
    return null;
  }

  private boolean hasReturn(StatContext stat) {
    if (stat instanceof ReturnStatContext || stat instanceof ExitStatContext) {
      return true;
    } else if (stat instanceof IfStatContext) {
      return hasReturn(((IfStatContext) stat).stat(0))
          && hasReturn(((IfStatContext) stat).stat(1));
    } else if (stat instanceof WhileStatContext) {
      return hasReturn(((WhileStatContext) stat).stat());
    } else if (stat instanceof ScopingStatContext) {
      return hasReturn(((ScopingStatContext) stat).stat());
    } else if (stat instanceof CompStatContext) {
      for (StatContext c : ((CompStatContext) stat).stat()) {
        if (hasReturn(c)) {
          return true;
        }
      }
    }
    return false;
  }

}
