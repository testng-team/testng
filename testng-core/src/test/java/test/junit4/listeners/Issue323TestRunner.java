package test.junit4.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class Issue323TestRunner extends SimpleBaseTest {
  @Test
  public void testMethod() {
    TestNG testng = create(Issue323TestSample.class);
    Issue323JUnitInvocationListener listener = new Issue323JUnitInvocationListener();
    testng.addListener((ITestNGListener) listener);
    testng.setJUnit(true);
    testng.run();
    assertThat(Issue323JUnitInvocationListener.messages)
        .containsExactly("beforeInvocation", "afterInvocation");
  }
}
