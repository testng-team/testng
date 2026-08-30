package test.timeout;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class TimeOutIntegrationTest extends SimpleBaseTest {

  @Test(description = "https://github.com/cbeust/testng/issues/811")
  public void testTimeOutWhenParallelIsTest() {
    TestNG tng = create(TimeOutWithParallelSample.class);
    tng.setParallel(XmlSuite.ParallelMode.TESTS);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).containsExactly("myTestMethod");
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).isEmpty();

    // The statuses above hold whether or not the time-out was enforced: a method that overruns is
    // failed once it returns, so a run that never cut it short reports exactly the same thing. The
    // duration is what distinguishes them, and it is what GITHUB-811 was actually about. The bound
    // is twice the time-out, as in the GITHUB-2009 check for parallel="methods": an enforced
    // time-out returns at about 1s and an unenforced one at the sample's 3s sleep.
    ITestResult failed = listener.getResult("myTestMethod");
    assertThat(failed.getEndMillis() - failed.getStartMillis())
        .as("myTestMethod must be cut short by its time-out rather than run to completion")
        .isLessThan(2_000L);
  }

  @Test(description = "https://github.com/cbeust/testng/issues/1314")
  public void testGitHub1314() {
    TestNG tng = create(GitHub1314Sample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("iWorkWell");
    assertThat(listener.getFailedMethodNames()).containsExactly("iHangHorribly");
    assertThat(listener.getSkippedMethodNames()).containsExactly("iAmNeverRun");
  }
}
