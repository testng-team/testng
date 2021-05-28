package test.jason;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class Main extends MainBase {
  public static boolean m_passed = false;

  @AfterClass
  public void afterClass() {
    m_passed = true;
  }

  @Test(description = "This test is run")
  public void test1() throws InterruptedException {
  }
}
