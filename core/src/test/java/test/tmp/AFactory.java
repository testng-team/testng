package test.tmp;

import org.testng.annotations.Factory;

public class AFactory {
  @Factory
  public Object[] create() {
    return new Object[] { new A(), new AA() };
  }

}
