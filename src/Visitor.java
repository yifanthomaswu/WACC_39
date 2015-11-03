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

    ParseTree tree = parser.program(); // begin parsing at program rule

    // build and run my custom visitor
    MyVisitor visitor = new MyVisitor();
    visitor.visit(tree);

  }
}
