package wacc.visitor.semantic_error.utils;

public abstract class Type {

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();

}