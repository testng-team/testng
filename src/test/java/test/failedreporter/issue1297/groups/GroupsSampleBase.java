package test.failedreporter.issue1297.groups;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

public class GroupsSampleBase {
  @BeforeTest(groups = "run")
  public void baseBeforeTest() {}

  @BeforeClass(alwaysRun = true)
  public void baseBeforeClassAlwaysRun() {}

  @BeforeMethod
  public void baseBeforeMethod() {}

  @AfterClass
  public void baseAfterClass() {}
}
