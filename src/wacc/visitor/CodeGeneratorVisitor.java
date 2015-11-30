package wacc.visitor;

import antlr.*;
import antlr.BasicParserBaseVisitor;
import wacc.visitor.utils.CodeWriter;

public class CodeGeneratorVisitor extends BasicParserBaseVisitor<Void> {

  private final CodeWriter writer;

  public CodeGeneratorVisitor(CodeWriter writer) {
    this.writer = writer;
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
