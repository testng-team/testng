package test.listeners.issue2456;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.ExitCode;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2456")
  public void testEavesdroppingIntoDataProviderExceptions() {
    TestNG testng = create(TestClassSample.class);
    SimpleErrorSniffingListener listener = new SimpleErrorSniffingListener();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getTestMethod().getName()).isEqualTo("sampleTest");
    assertThat(listener.getDataProvider().getName()).isEqualTo("getData");
    assertThat(listener.getException()).isInstanceOf(TestClassSample.TestCaseFailedException.class);
    assertThat(testng.getStatus()).isEqualTo(ExitCode.SKIPPED);
  }
}
