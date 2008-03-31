package test.tmp;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class A {

  @Parameters("foo")
  @Test
  public void f1(String param) {
    System.out.println(param);
  }
  
}
