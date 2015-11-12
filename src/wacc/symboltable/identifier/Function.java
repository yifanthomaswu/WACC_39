package symbolTable.identifier;

import symbolTable.SymbolTable;

public class Function extends Identifier {
  Type returnType;
  Param params[];
  SymbolTable table;
}