package test.sample;

import org.testng.annotations.*;

public class Basic1 {
  static private int m_count = 0;
  
  public static void incrementCount() {
    m_count++;
  }
  
  public static int getCount() {
    return m_count;
  }
  
  @Configuration(beforeTestMethod = true)
  public void beforeTestMethod() {
    incrementCount();
  }
  
  @Test(groups = { "basic1" } )
  public void basic1() {
    assert getCount() > 0 : "COUNT WAS NOT INCREMENTED";
  }
  
  static private void ppp(String s) {
    System.out.println("[Basic1] " + s);
  }
}