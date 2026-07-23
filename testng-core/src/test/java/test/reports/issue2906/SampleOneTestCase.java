package test.reports.issue2906;

import static org.assertj.core.api.Assertions.fail;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class SampleOneTestCase {

  @Test
  public void firstPassingTestCase() {
    Reporter.log("firstPassingTestCase() ran");
  }

  @Test
  public void firstFailingTestCase() {
    fail();
  }
}
