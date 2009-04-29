package test.tmp;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class B extends Base {

  @Factory
  public static Object[] create() {
    return new Object[] {
        new A("abc"),
        new A("def")
    };
  }
  
  @Test
  public void f() {
    
  }
}
