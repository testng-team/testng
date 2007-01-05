package test.tmp;

import org.testng.annotations.Factory;

public class B {
  
  @Factory
  public Object[] create() {
    return new Object[] {
        new A(), new A()
    };
  }
}
