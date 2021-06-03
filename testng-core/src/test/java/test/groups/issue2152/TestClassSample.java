package test.groups.issue2152;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {"Group1", "Group2", "Group3", "Group4"})
public class TestClassSample {
  static List<String> logs = new ArrayList<>();

  @BeforeMethod
  public void setUp() {
    logs.add("setup");
  }

  @AfterMethod
  public void tearDown(ITestResult result) {
    logs.add("teardown");
  }

  public void test1() {
    logs.add("test1");
  }
}
