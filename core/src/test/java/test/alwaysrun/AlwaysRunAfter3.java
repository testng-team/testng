package test.alwaysrun;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AlwaysRunAfter3 {

  private static boolean m_success = false;

  @BeforeMethod
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethodShouldBeCalled() {
    m_success = true;
  }

  @Test
  public void dummy() {}

  public static boolean success() {
    return m_success;
  }
}
