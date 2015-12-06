package wacc.visitor.code_generator;

public enum Reg {
  R4, R5, R6, R7, R8;

  private static final Reg[] values = values();

  public Reg getNext() {
    return values[this.ordinal() + 1];
  }

  public Reg getPrevious() {
    return values[this.ordinal() - 1];
  }

  @Override
  public String toString() {
    return this.name().toLowerCase();
  };

}
