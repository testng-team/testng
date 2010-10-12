package test.alwaysrun;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class AlwaysRunAfter2 {

  private static boolean m_success = true;

  @BeforeClass
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  // Should not be run
  @AfterClass
  public void tearDown() {
    m_success = false;
  }

  static public boolean success() {
    return m_success;
  }
}
