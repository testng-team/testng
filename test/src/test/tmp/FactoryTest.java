package test.tmp;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test
public class FactoryTest {
  
  @Test
  public void f2() {
    
  }
  
  @Factory
  public Object[] init() {
    return new Object[] { 
//        new B("Test1"),
//        new B("Test2")
    };
  }
}
