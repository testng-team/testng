package test.triangle;

import org.testng.annotations.*;

/**
 * This class
 * 
 * @author cbeust
 */
public class Base {
  protected boolean m_isInitialized = false;
  
  @Configuration(beforeSuite = true)
  public void beforeSuite() {
    CountCalls.numCalls = 0;
  }  
  
  @Configuration(beforeTestClass = true)
  public void initBeforeTestClass() {
    m_isInitialized = true;
  }
  
  @Configuration(afterTestClass = true)
  public void postAfterTestClass() {
    CountCalls.incr();
  }

  private static void ppp(String s) {
    System.out.println("[Base] " + s);
  }
}
