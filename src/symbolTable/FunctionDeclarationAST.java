package symbolTable;

public class FunctionDeclarationAST {
    String returntypename;
    String funcname;
    ParameterASTlist parameters;
    Function funcObj;

    public void checkFunctionNameAndReturnType() {
        t = ST.lookupAll(returntypename);
        f = ST.lookup(funcname);
        if(T == null) {
            System.out.println("unknown type " + returntypename);
            System.exit(200);
        } else if(!(t instanceof Type)) {
            System.out.println(returntypename + " is not a type");
            System.exit(200);
        } else if(!(t.isReturnable())) {
            System.out.println("cannot return objects " + returntypename);
            System.exit(200); 
        } else if(f == null) {
            System.out.println(funcname + " is already declared");
            System.exit(200);
        } else {
            funcObj = new Function(t);
            ST.add(funcname, funcObj);
        }
    }

    public void check() {
        checkFunctionNameAndReturnType();

        ST = new SymbolTable(ST);
        funcObj.symtab = ST;

        for(Parameter p:parameters) {
            p.check();
            funcObj.formals.append(p.paramObj);
        }

        ST = ST.encSymTable
    }

}
