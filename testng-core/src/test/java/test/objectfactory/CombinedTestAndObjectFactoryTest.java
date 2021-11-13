package test.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class CombinedTestAndObjectFactoryTest extends SimpleBaseTest {

  @Test
  void combinedTestAndObjectFactory() {
    TestNG tng = create(CombinedTestAndObjectFactorySample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly("isConfigured");
  }
}
