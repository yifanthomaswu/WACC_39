import java.util.*;
import antlr.*;

public class MyVisitor extends BasicParserBaseVisitor<Void> {

  public Void visitProgram(BasicParser.ProgramContext ctx) {
    System.out.println("Hi");
    return null;
  }

}
