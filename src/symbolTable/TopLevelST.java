package symbolTable;

import SymbolTable.java
import Identifier.java

public class TopLevelST {
    TOP_ST = new SymbolTable(NULL);

    ST = TOP_ST; //Current Symbol Table
    charT = new Scalar(0, 255);
    ST.add("int", new Scalar(-2147483648, 2147483647));
    ST.add("char", charT);
    ST.add("bool", new Scalar(0, 1));
    ST.add("string", new Array(charT, 2);

    doubleT = ST.lookup("double");
    ST.add("sin", new Function(returntype=doubleT,formals=new Param(doubleT));
    arrayT = ST.lookup("array")
    ST.add("len", new Function(returntype="int", formals=new Param(arrayT)));
    ST.add("!", new Function(returntype="bool", formals=new Param("bool")));
}


