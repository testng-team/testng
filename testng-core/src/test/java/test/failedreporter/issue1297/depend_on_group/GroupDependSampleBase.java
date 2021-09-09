package test.failedreporter.issue1297.depend_on_group;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import test.failedreporter.FailedReporterTest;

public class GroupDependSampleBase {
  @BeforeTest(groups = FailedReporterTest.DEPENDENT_GROUP)
  public void baseBeforeTest() {}

  @BeforeClass(alwaysRun = true)
  public void baseBeforeClassAlwaysRun() {}

  @BeforeMethod
  public void baseBeforeMethod() {}

  @AfterClass
  public void baseAfterClass() {}
}
