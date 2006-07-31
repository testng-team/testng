package test.alwaysrun;

import org.testng.annotations.Configuration;

public class AlwaysRunAfter2 {

  private static boolean m_success = true;

  @Configuration(beforeTestClass = true)
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }
  
  // Should not be run
  @Configuration(afterTestClass = true)
  public void tearDown() {
    m_success = false;
  }
  
  static public boolean success() {
    return m_success;
  }
}
