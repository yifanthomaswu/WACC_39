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
        file.println(".text");
        file.println();
        file.println(".global main");
        file.println("main:");
        file.println("PUSH {lr}");
        visitChildren(ctx);
        file.println("LDR r0, =0");
        file.println("POP {pc}");
        file.println(".ltorg");

        return null;
    }

    @Override
    public Void visitExitStat(BasicParser.ExitStatContext ctx) {
        file.println("LDR r4, =" + ctx.expr().getText());
        file.println("MOV r0, r4");
        file.println("BL exit");
        return null;
    }
}
