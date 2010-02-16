package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

public class B {
  @Factory
  public Object[] factory() {
    return new Object[] {
        new A(1), new A(2)
    };
  }

  @BeforeClass
  public void beforeB() {
    System.out.println("Before B");
  }
}

