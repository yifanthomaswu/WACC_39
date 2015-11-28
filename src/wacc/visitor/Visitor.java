package wacc.visitor;

// import ANTLR's runtime libraries
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

// import antlr package (your code)
import antlr.*;

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

    //Start code generation
    CodeGeneratorVisitor codeGeneratorVisitor = new CodeGeneratorVisitor();
    codeGeneratorVisitor.visit(tree);

  }
}
