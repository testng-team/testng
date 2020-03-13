package test.factory.issue553;

import org.testng.annotations.Factory;

public abstract class Base {
  @Factory
  public Object[] createTests() {
    return new Object[] {new Inner()};
  }

  public class Inner {
    @Factory
    public Object[] createTests() {
      return new Object[0];
    }
  }
}
