package test.conffailure;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeTestClassVerification {

  private static boolean m_success1 = false;
  private static boolean m_success2 = false;

  // Should be run even though ClassWithFailedBeforeTestClass failed in its configuration
  @BeforeClass
  public void setUpShouldPass() {
    m_success1 = true;
  }

  // Should be run even though ClassWithFailedBeforeTestClass  failed in its configuration
  @AfterClass
  public void tearDown() {
    m_success2 = true;
  }

  // Adding this method or @Configuration will never be invoked
  @Test
  public void dummy() {

  }

  static public boolean success() {
    return m_success1 && m_success2;
  }
}
