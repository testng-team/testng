package test.alwaysrun;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AlwaysRunAfter1 {
  private static boolean m_success = false;

  @BeforeClass
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  @AfterClass(alwaysRun = true)
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
