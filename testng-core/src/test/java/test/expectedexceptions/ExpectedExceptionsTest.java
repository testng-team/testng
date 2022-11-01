package test.expectedexceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.ExitCode;
import test.BaseTest;
import test.expectedexceptions.issue2788.TestClassSample;
import test.expectedexceptions.issue2788.TestClassSample.Local;

public class ExpectedExceptionsTest extends BaseTest {

  @Test
  public void expectedExceptionsDeprecatedSyntax() {
    runTest(
        "test.expectedexceptions.SampleExceptions",
        new String[] {"shouldPass"},
        new String[] {"shouldFail1", "shouldFail2", "shouldFail3"},
        new String[] {});
  }

  @Test(description = "GITHUB-2788")
  public void expectedExceptionsWithProperStatusPassedToListener() {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] {TestClassSample.class});
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(ExitCode.FAILED);
    assertThat(Local.getInstance().isPass()).isFalse();
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
    addClass(test.expectedexceptions.github1409.TestClassSample.class);
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
