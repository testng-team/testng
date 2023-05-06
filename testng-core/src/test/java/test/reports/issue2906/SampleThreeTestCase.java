package test.reports.issue2906;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class SampleThreeTestCase {

  @Test
  public void thirdPassingTestCase() {
    Reporter.log("thirdPassingTestCase() ran");
  }

  @Test
  public void thirdFailingTestCase() {
    Assert.fail();
  }
}
