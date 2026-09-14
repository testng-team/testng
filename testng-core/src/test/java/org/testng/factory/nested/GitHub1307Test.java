package org.testng.factory.nested;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.factory.samples.nested.FactoryWithAnonymousTestsSample;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class GitHub1307Test extends SimpleBaseTest {

  @Test(description = "GITHUB-1307")
  public void testGitHub1307() {
    TestNG tng = create(FactoryWithAnonymousTestsSample.class);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly("test");
  }
}
