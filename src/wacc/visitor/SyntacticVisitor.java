package wacc.visitor;

import java.util.LinkedList;
import java.util.List;

import antlr.*;
import antlr.BasicParser.*;

public class SyntacticVisitor extends BasicParserBaseVisitor<Void> {

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
//      List<StatContext> stats = ((CompStatContext) stat).stat();
//      return checkBlock(stats);
      for (StatContext c : ((CompStatContext) stat).stat()) {
        if (hasReturn(c)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean checkBlock(List<StatContext> stats) {
    for (int i = 0; i < stats.size(); i++) {
      if (stats.get(i) instanceof ReturnStatContext
          || stats.get(i) instanceof ExitStatContext)
        return true;
    }
    List<StatContext> list = new LinkedList<>();
    for (int i = 0; i < stats.size(); i++) {
      if (stats.get(i) instanceof ScopingStatContext
          || stats.get(i) instanceof CompStatContext
          || stats.get(i) instanceof IfStatContext)
        list.add(stats.get(i));
    }
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) instanceof ScopingStatContext) {
        if (hasReturn(((ScopingStatContext) list.get(i)).stat()))
          return true;
      } else if (list.get(i) instanceof CompStatContext) {
        if (checkBlock(((CompStatContext) list.get(i)).stat()))
          return true;
      } else if (list.get(i) instanceof IfStatContext) {
        if (hasReturn(((IfStatContext) list.get(i)).stat(0))
            && hasReturn(((IfStatContext) list.get(i)).stat(1)))
          return true;
      }
    }
    return false;
  }

}
