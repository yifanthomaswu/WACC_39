package symbolTable;

public class Identifier {
}

class Type extends Identifier {
  public boolean isDeclarable() {
    return true;
  }
}

class Variable extends Identifier {
  Type type;
}

class Param extends Identifier {
  Type type;
}

class Scalar extends Type {
  int min;
  int max;

  public Scalar(int min, int max) {
    this.min = min;
    this.max = max;
  }
}

class Array extends Type {
  Type type;
  int numOfElems;
}

class Function extends Identifier {
  Type returnType;
  Param params[];
  SymbolTable table;
}
