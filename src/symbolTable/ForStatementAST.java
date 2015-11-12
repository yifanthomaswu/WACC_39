package symbolTable;

public class ForStatementAST {
    String forloopvarname;
    ExpressionAST expr1;
    ExpressionAST expr2;
    StatementAST s;
    boolean ascending;
    Variable forvar;
   
    public void check() {
        expr1.check();
        expr2.check();

        f = ST.lookup(forloopvarname);
        if(f == null) {
            System.out.println("for variable not declared locally");
            System.exit(200);
        } else if(!(f instanceof variable)) {
            System.out.println("for variable not a variable");
            System.exit(200);
        } else if(!(scalarType(f.type, "int")) && !(scalarType(f.type, "bool")) 
            && !(scalarType(f.type, "char") && ...)) {
            System.out.println("For variable not of supported type");
            System.exit(200);
        } else if(!(assignCompat(f.type, expr1.type))) {
            System.out.println("From expression type not compatible with for variable");
            System.exit(200);
        } else if(!(assignCompat(f.type, expr2.type))) {
            System.out.println("To expression type not compatible with for variable");
            System.exit(200);
        } else {
            s.check;
            forvar = f;
        }
    }    
}
