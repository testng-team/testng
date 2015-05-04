package test.reports;

import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import org.testng.ITestResult;

/**
 * Make sure that Reporter.log() in listeners don't get discarded.
 */
public class ReporterLogTest extends SimpleBaseTest {

  @Test
  public void shouldLogFromListener() {
    TestNG tng = create(ReporterLogSampleTest.class);
    tng.run();
    boolean success = false;
    for(String s : ReporterLogSampleTest.output) {
      if (s.contains("Log from listener")) {
        success = true;
        break;
      }
    }
    Assert.assertTrue(success);
  }

  @Test
  public void we_should_be_able_to_get_output_for_specific_results() {
    String expectedLog1 = "asdfasdf";
    String expectedLog2 = "ffffzzzzsdf";
    ITestResult result1 = new org.testng.internal.TestResult();
    ITestResult result2 = new org.testng.internal.TestResult();
    Reporter.setCurrentTestResult(result1);
    Reporter.log(expectedLog1);
    Reporter.setCurrentTestResult(result2);
    Reporter.log(expectedLog2);
    assertEquals(expectedLog1, Reporter.getOutput(result1).get(0));
    assertEquals(expectedLog2, Reporter.getOutput(result2).get(0));
  }
}
