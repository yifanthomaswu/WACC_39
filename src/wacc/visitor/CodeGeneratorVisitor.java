package wacc.visitor;

import antlr.BasicParser;
import antlr.BasicParserBaseVisitor;

import java.io.*;

/**
 * Created by md3414 on 28/11/15.
 */
public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

    private PrintWriter file;

    public CodeGeneratorVisitor(PrintWriter file) {
        this.file = file;
    }
    
    @Override
    public Void visitProgram(BasicParser.ProgramContext ctx) {
        return null;
    }
}
