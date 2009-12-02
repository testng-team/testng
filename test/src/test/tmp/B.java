package test.tmp;

import org.testng.annotations.Factory;

public class B {
  @Factory
  public Object[] f() {
    return new Object[] { new A(), new A() };
  }
}
