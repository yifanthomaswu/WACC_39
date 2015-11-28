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
    return null;
  }
}
