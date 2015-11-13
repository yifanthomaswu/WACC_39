package wacc.visitor;

import org.antlr.v4.runtime.Token;

@SuppressWarnings("serial")
public class SemanticErrorException extends RuntimeException {

  public SemanticErrorException(Token token, String msg) {
    super("Semantic Error at " + token.getLine() + ":" +
        token.getCharPositionInLine() + " -- " + msg);
  }

}
