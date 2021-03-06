package wacc.visitor.syntactic_error;

import org.antlr.v4.runtime.*;

public class SyntacticErrorListener extends BaseErrorListener {

  public static final SyntacticErrorListener INSTANCE =
      new SyntacticErrorListener();

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
      int line, int charPositionInLine, String msg, RecognitionException e) {
    if (msg.contains("Syntactic Error")) {
      System.out.println(msg);
    } else {
      System.out.println("Syntactic Error at " + line + ":" + charPositionInLine
          + " -- " + msg);
    }
  }
}
