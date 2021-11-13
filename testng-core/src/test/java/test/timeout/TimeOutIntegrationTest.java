package test.timeout;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
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
