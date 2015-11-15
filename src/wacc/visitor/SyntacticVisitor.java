package wacc.visitor;

import antlr.BasicParser;
import antlr.BasicParserBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

/**
 * Created by md3414 on 15/11/15.
 */
public class SyntacticVisitor extends BasicParserBaseVisitor<Boolean> {

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
            BasicParser.IfThenElseStatContext ctx = (BasicParser.IfThenElseStatContext)stat;
            return hasReturn(ctx.stat(0)) && hasReturn(ctx.stat(1));
        }
        else if (stat instanceof BasicParser.WhileStatContext)
        {
            BasicParser.WhileStatContext ctx = (BasicParser.WhileStatContext)stat;
            return hasReturn(ctx.stat());
        }
        else if (stat instanceof BasicParser.ScopingStatContext)
        {
            BasicParser.ScopingStatContext ctx = (BasicParser.ScopingStatContext)stat;
            return hasReturn(ctx.stat());
        }
        else if (stat instanceof BasicParser.CompStatContext)
        {
            BasicParser.CompStatContext ctx = (BasicParser.CompStatContext)stat;
            List<BasicParser.StatContext> stats = ctx.stat();
            for (BasicParser.StatContext st : stats)
            {
                if ((stat instanceof BasicParser.IfThenElseStatContext) ||
                        (stat instanceof BasicParser.WhileStatContext) ||
                        (stat instanceof BasicParser.ScopingStatContext) ||
                        (stat instanceof BasicParser.CompStatContext) ||
                        (stat instanceof BasicParser.ReturnStatContext) ||
                        (stat instanceof BasicParser.ExitStatContext)
                        )
                {
                    if (!hasReturn(stat))
                        return false;
                }
            }
            return true;
        }
        return false;
    }
}