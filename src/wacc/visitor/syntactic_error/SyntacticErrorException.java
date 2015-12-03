package wacc.visitor.syntactic_error;

import org.antlr.v4.runtime.Token;

@SuppressWarnings("serial")
public class SyntacticErrorException extends RuntimeException {

  public SyntacticErrorException(Token token, String msg) {
    super("Syntactic Error at " + token.getLine() + ":"
        + token.getCharPositionInLine() + " -- " + msg);
  }

}
