package wacc.visitor;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import wacc.visitor.utils.CodeWriter;

// import antlr package (your code)
import antlr.*;

import java.io.File;
import java.io.PrintWriter;

public class Visitor {
  public static void main(String[] args) throws Exception {

    // create a CharStream that reads from standard input
    ANTLRInputStream input = new ANTLRInputStream(System.in);

    // create a lexer that feeds off of input CharStream
    BasicLexer lexer = new BasicLexer(input);

    // create a buffer of tokens pulled from the lexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // create a parser that feeds off the tokens buffer
    BasicParser parser = new BasicParser(tokens);

    // add an error listener for custom syntactic error messages
    parser.removeErrorListeners();
    parser.addErrorListener(SyntacticErrorListener.INSTANCE);

    // begin parsing at program rule
    ParseTree tree = parser.program();

    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();

    // build and run SyntacticVisitor, check functions ended with return or exit
    try {
      SyntacticVisitor syntacticVisitor = new SyntacticVisitor();
      syntacticVisitor.visit(tree);
    } catch (SyntacticErrorException e) {
      System.out.println(e.getMessage());
      numberOfSyntaxErrors++;
    }

    // exit with code 100 if syntactic error exits
    if (numberOfSyntaxErrors > 0) {
      System.out.println(numberOfSyntaxErrors
          + " parser error(s) detected, no further compilation attempted.");
      System.exit(100);
    }

    // build and run SemanticVisitor, exit with code 200 if semantic error exits
    try {
      SemanticVisitor semanticVisitor = new SemanticVisitor();
      semanticVisitor.visit(tree);
    } catch (SemanticErrorException e) {
      System.out.println(e.getMessage());
      System.exit(200);
    }

    // get name of output file
    String filename  = new File(args[0]).getName().replaceFirst(".wacc", ".s");
    // create new file for writing
    PrintWriter file = new PrintWriter(filename, "UTF-8");
    // Start code generation
    CodeWriter writer = new CodeWriter(file);
    CodeGeneratorVisitor codeGeneratorVisitor = new CodeGeneratorVisitor(writer);
    codeGeneratorVisitor.visit(tree);
    writer.writeToFile();
    // close file
    file.close();

  }
}
