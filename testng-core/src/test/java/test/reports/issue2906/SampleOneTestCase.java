package test.reports.issue2906;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class SampleOneTestCase {

  @Test
  public void firstPassingTestCase() {
    Reporter.log("firstPassingTestCase() ran");
  }

  @Test
  public void firstFailingTestCase() {
    Assert.fail();
  }
}
