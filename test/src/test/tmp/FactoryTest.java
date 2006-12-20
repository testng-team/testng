package test.tmp;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test
public class FactoryTest {
  
  @Factory
  public Object[] init() {
    return new Object[] { 
        new B(),
        new B(),
    };
  }
}

