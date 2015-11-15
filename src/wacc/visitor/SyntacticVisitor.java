package wacc.visitor;

import antlr.BasicParser;
import antlr.BasicParserBaseVisitor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by md3414 on 15/11/15.
 */
public class
SyntacticVisitor extends BasicParserBaseVisitor<Boolean> {

    @Override
    public Boolean visitFunc(BasicParser.FuncContext ctx) {
//        System.out.println("Rule indx: " + ctx.getChildCount()); //ctx.stat().getChild(ctx.stat().getChildCount() - 1).toString());
        if (!hasReturn(ctx.stat()))
        {
            ////Error
            String msg = "Function " + ctx.ident().getText() + " is not ended with a return or an exit statement.";
            throw new SyntacticErrorException(ctx.getStart(), msg);
        }
        return visitChildren(ctx);
    }

    private boolean hasReturn(BasicParser.StatContext stat) {
        if (stat instanceof BasicParser.ReturnStatContext)
        {
            return true;
        }
        else if (stat instanceof BasicParser.ExitStatContext)
        {
            return true;
        }
        else if (stat instanceof BasicParser.IfThenElseStatContext)
        {
            return hasReturn(((BasicParser.IfThenElseStatContext)stat).stat(0)) &&
                    hasReturn(((BasicParser.IfThenElseStatContext)stat).stat(1));
        }
        else if (stat instanceof BasicParser.WhileStatContext)
        {
            return hasReturn(((BasicParser.WhileStatContext)stat).stat());
        }
        else if (stat instanceof BasicParser.ScopingStatContext)
        {
            return hasReturn(((BasicParser.ScopingStatContext)stat).stat());
        }
        else if (stat instanceof BasicParser.CompStatContext)
        {
            List<BasicParser.StatContext> stats = ((BasicParser.CompStatContext)stat).stat();
            return checkBlock(stats);
        }
        return false;
    }

    private boolean checkBlock(List<BasicParser.StatContext> stats) {
        for (int i = 0; i < stats.size(); i++)
        {
            if (stats.get(i) instanceof BasicParser.ReturnStatContext || stats.get(i) instanceof BasicParser.ExitStatContext)
                return true;
        }
        List<BasicParser.StatContext> list = new LinkedList<>();
        for (int i = 0; i < stats.size(); i++)
        {
            if (stats.get(i) instanceof BasicParser.ScopingStatContext ||
                    stats.get(i) instanceof BasicParser.CompStatContext ||
                    stats.get(i) instanceof BasicParser.IfThenElseStatContext)
                list.add(stats.get(i));
        }
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) instanceof BasicParser.ScopingStatContext) {
                if (hasReturn(((BasicParser.ScopingStatContext)list.get(i)).stat()))
                    return true;
            }
            else if (list.get(i) instanceof BasicParser.CompStatContext)
            {
                if (checkBlock(((BasicParser.CompStatContext)list.get(i)).stat()))
                    return true;
            }
            else if (list.get(i) instanceof BasicParser.IfThenElseStatContext)
            {
                if (hasReturn(((BasicParser.IfThenElseStatContext)list.get(i)).stat(0)) &&
                        hasReturn(((BasicParser.IfThenElseStatContext)list.get(i)).stat(1)))
                    return true;
            }
        }
        return false;
    }
}