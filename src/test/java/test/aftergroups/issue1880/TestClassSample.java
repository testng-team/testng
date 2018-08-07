package test.aftergroups.issue1880;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class TestClassSample {

  @BeforeGroups(groups = "123")
  public void before() throws Exception {
    throw new Exception("forcing a failure");
  }

  @Test(groups = "123")
  public void test() {

  }

  @AfterGroups(groups = "123", alwaysRun = true)
  public void after() {
  }

}