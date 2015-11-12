package symbolTable

public boolean scalarType(Type type, String name) {
    return type == TOP_ST.lookup(name);
}

public class IfStatementAST {
    ExpressionAST expr;
    StatementAST s1;
    StatementAST s2;

    public void check() {
        expr.check();
        if(!scalarType(expr.type, "bool") {
            System.out.println("if condition expression not a boolean");
            System.exit(200);
        }
        s1.check();
        if(s2 != null) {
            s2.check();
        }
        
}



