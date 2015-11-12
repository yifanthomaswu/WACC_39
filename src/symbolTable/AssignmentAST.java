package symbolTable;

public class AssignmentAST {
    String varname;
    ExpressionAST expr;
    SymbolTable ST;
    Variable varObj;

    public void Check() {
        Identifier V = ST.lookupAll(varname);
        expr.Check();
        if (V == null) {
            System.out.println("unknown variable");
            System.exit(200);
        }
        else if (!(V instanceof Variable)) {
            System.out.println("not a variable");
            System.exit(200);
        }
        else if (!Utils.assignCompat(((Variable) V).type, expr.type)) {
            System.out.println("lhs and rhs not compatible");
            System.exit(200);
        }
        else {
            varObj = (Variable) V;
        }
    }

}
