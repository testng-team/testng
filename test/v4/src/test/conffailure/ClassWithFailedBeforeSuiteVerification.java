package test.conffailure;

import org.testng.annotations.Configuration;

public class ClassWithFailedBeforeSuiteVerification {

  private static boolean m_success1 = true;
  private static boolean m_success2 = true;

  // Should not be run because beforeSuite failed on the other class
  @Configuration(beforeTestClass = true)
  public void setUp() {
    m_success1 = false;
  }

  // Should not be run because beforeSuite failed on the other class
  @Configuration(afterTestClass = true)
  public void tearDown() {
    m_success2 = false;
  }
  
  static public boolean success() {
    return m_success1 && m_success2;
  }
}
