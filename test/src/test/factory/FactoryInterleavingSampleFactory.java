package test.factory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;

public class FactoryInterleavingSampleFactory {
  @Factory
  public Object[] factory() {
    return new Object[] {
        new FactoryInterleavingSampleA(1), new FactoryInterleavingSampleA(2)
    };
  }

  @BeforeClass
  public void beforeB() {
    System.out.println("Before B");
  }
}

