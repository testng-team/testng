package test.alwaysrun;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class AlwaysRunAfter1 {
  private static boolean m_success = false;

  @Configuration(beforeTestClass = true)
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }
  
  @Configuration(afterTestClass = true, alwaysRun = true)
  public void tearDown() {
    m_success = true;
  }
  
  // Adding this method or @Configuration will never be invoked
  @Test
  public void dummy() {
    
  }
  
  static public boolean success() {
    return m_success;
  }
}
