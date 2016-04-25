package test.reports;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.List;

/**
 * Make sure that Reporter.log() in listeners don't get discarded.
 */
public class ReporterLogTest extends SimpleBaseTest {

  @Test
  public void shouldLogFromListenerOnSuccess() {
    TestNG tng = create(ReporterLogSuccessSampleTest.class);
    tng.run();
    List<String> output = Reporter.getOutput();
    //System.out.println(output);
    Assert.assertTrue(contains(output, "Listener: onTestSuccess"), "Reporter should log from onTestSuccess listener");
  }
  
  @Test
  public void shouldLogFromListenerOnSkip() {
    TestNG tng = create(ReporterLogSkippedSampleTest.class);
    tng.run();
    List<String> output = Reporter.getOutput();
    //System.out.println(output);
    Assert.assertTrue(contains(output, "Listener: onTestSkipped"), "Reporter should log from onTestSkipped listener");
  }

  @Test
  public void shouldLogFromListenerOnFailure() {
    TestNG tng = create(ReporterLogFailureSampleTest.class);
    tng.run();
    List<String> output = Reporter.getOutput();
    //System.out.println(output);
    Assert.assertTrue(contains(output, "Listener: onTestFailure"), "Reporter should log from onTestFailure listener");
  }
  
  private boolean contains(List<String> output, String logMessage) {
    for (String s : output) {
      if (s.contains(logMessage)) {
        return true;
      }
    }
    return false;
  }
}
