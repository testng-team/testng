package test.reports.issue2906;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class SampleTwoTestCase {

  @Test
  public void secondPassingTestCase() {
    Reporter.log("secondPassingTestCase() ran");
  }

  @Test
  public void secondFailingTestCase() {
    Assert.fail();
  }
}
