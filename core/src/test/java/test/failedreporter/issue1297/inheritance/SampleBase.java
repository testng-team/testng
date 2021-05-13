package test.failedreporter.issue1297.inheritance;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

public class SampleBase {
  @BeforeTest
  public void baseBeforeTest() {}

  @BeforeClass
  public void baseBeforeClass() {}

  @BeforeMethod
  public void baseBeforeMethod() {}

  @AfterClass
  public void baseAfterClass() {}
}
