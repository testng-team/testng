package test.reports;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.List;

/**
 * Make sure that Reporter.log() in listeners don't get discarded.
 */
public class ReporterLogTest extends SimpleBaseTest {

  @DataProvider
  public static Object[][] dp() {
    return new Object[][]{
        new Object[]{ReporterLogSuccessSample.class, "Listener: onTestSuccess"},
        new Object[]{ReporterLogSkippedSample.class, "Listener: onTestSkipped"},
        new Object[]{ReporterLogFailureSample.class, "Listener: onTestFailure"}
    };
  }

  @Test(dataProvider = "dp")
  public void shouldLogFromListener(Class<?> testClass, String value) {
    TestNG tng = create(testClass);
    tng.run();
    List<String> output = Reporter.getOutput();
    Assert.assertTrue(contains(output, value));
  }

  private static boolean contains(List<String> output, String logMessage) {
    for (String s : output) {
      if (s.contains(logMessage)) {
        return true;
      }
    }
    return false;
  }
}
