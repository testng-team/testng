package test.reports.issue2906;

import static org.assertj.core.api.Assertions.fail;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class SampleThreeTestCase {

  @Test
  public void thirdPassingTestCase() {
    Reporter.log("thirdPassingTestCase() ran");
  }

  @Test
  public void thirdFailingTestCase() {
    fail();
  }
}
