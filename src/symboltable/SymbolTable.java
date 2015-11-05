package symboltable;

import java.util.Dictionary;

public class SymbolTable {
	SymbolTable encSymTable;
	Dictionary<String, String> dict;	
	

	public SymbolTable(SymbolTable st) {
		dict = new HashTable<String, String>;
		encSymTable = st;
    }
	
	public void add(String name, String obj) {
		dict.add(name, obj);
	}
	
	public void lookup(String name) {
		dict.get(name);
	}

}