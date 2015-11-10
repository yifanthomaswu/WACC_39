package symbolTable;

public class Identifier {}

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

class Package extends Identifier {
	SymbolTable table;
}

class Class extends Type {
	Class superClass;
	SymbolTable table;
}

class Function extends Identifier {
	Type returnType;
	Param params[];
	SymbolTable table;
}
