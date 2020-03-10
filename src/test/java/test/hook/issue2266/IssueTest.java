package test.hook.issue2266;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2266")
  public void ensureTestsCanBeRetriedViaCallBacks() {
    TestNG testng = create(TestClassSample.class);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getFailedTests()).isEmpty();
    assertThat(listener.getSkippedTests()).isEmpty();
    assertThat(listener.getPassedTests()).hasSize(1);
  }
}
