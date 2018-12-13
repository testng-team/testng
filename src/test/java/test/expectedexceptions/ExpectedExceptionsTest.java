package test.expectedexceptions;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import test.BaseTest;
import test.expectedexceptions.github1409.TestClassSample;

import java.util.Collection;
import java.util.List;

public class ExpectedExceptionsTest extends BaseTest {

  @Test
  public void expectedExceptionsDeprecatedSyntax() {
    runTest(
        "test.expectedexceptions.SampleExceptions",
        new String[] {"shouldPass"},
        new String[] {"shouldFail1", "shouldFail2", "shouldFail3"},
        new String[] {});
  }

  @Test
  public void expectedExceptions() {
    runTest(
        "test.expectedexceptions.SampleExceptions2",
        new String[] {"shouldPass", "shouldPass2", "shouldPass3", "shouldPass4"},
        new String[] {"shouldFail1", "shouldFail2", "shouldFail3", "shouldFail4"},
        new String[] {});
  }

  @Test
  public void expectedExceptionsMessage() {
    getFailedTests().clear();
    addClass(TestClassSample.class);
    run();
    Collection<List<ITestResult>> failedTests = getFailedTests().values();
    Assert.assertFalse(failedTests.isEmpty());
    ITestResult result = failedTests.iterator().next().get(0);
    String actual = result.getThrowable().getMessage().replaceAll("\\n", "");
    String expected =
        "The exception was thrown with the wrong message: expected \"expected\" but got \"actual\"";
    Assert.assertEquals(actual, expected);
  }
}
