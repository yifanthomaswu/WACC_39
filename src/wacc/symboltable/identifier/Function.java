package wacc.symboltable.identifier;

import wacc.symboltable.SymbolTable;

public class Function extends Identifier {
  Type returnType;
  Param params[];
  SymbolTable table;
}