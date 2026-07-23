package test.reports.issue2906;

import static org.assertj.core.api.Assertions.fail;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class SampleTwoTestCase {

  @Test
  public void secondPassingTestCase() {
    Reporter.log("secondPassingTestCase() ran");
  }

  @Test
  public void secondFailingTestCase() {
    fail();
  }
}
