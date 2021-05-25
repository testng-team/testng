package test.jason;

import org.testng.annotations.Test;

public class MainBase {

  @Test(description = "This test is never run but prevents AfterClass")
  public void checkReportsExist() {
  }
}
