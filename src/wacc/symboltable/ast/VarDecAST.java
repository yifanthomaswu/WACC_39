package wacc.symboltable.ast;

import symbolTable.Identifier;
import symbolTable.SymbolTable;

/**
 * Created by md3414 on 12/11/15.
 */
public class VarDecAST {
    String typename;
    String varname;
    Variable varObj;
    SymbolTable ST;

    public VarDecAST(String typename, String varname,
                     SymbolTable ST) {
        this.typename = typename;
        this.varname = varname;
        this.ST = ST;
    }

    void Check() {
        Identifier T = ST.lookupAll(typename);
        Identifier V = ST.lookup(varname);
        if (T == null) {
            System.out.println("unknown type");
            System.exit(200);
        }
        else if (!(T instanceof Type))
        {
            System.out.println("not a type");
            System.exit(200);
        }
        else if (!((Type) T).isDeclarable())
        {
            System.out.println("cannot declare");
            System.exit(200);
        }
        else if (V != null)
        {
            System.out.println("already declared");
            System.exit(200);
        }
        else
        {
            varObj = new Variable();
            ST.add(varname, varObj);
        }
    }
}
