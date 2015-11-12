package wacc.symboltable.identifier;

public class Scalar extends Type {
  int min;
  int max;

  public Scalar(int min, int max) {
    this.min = min;
    this.max = max;
  }
}