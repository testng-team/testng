package test.hook.issue2257;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2257")
  public void ensureConfigurationsCanBeRetriedViaCallBacks() {
    TestNG testng = create(TestClassSample.class);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.run();
    assertThat(listener.getConfigurationSkips()).isEmpty();
    assertThat(listener.getConfigurationFailures()).isEmpty();
    assertThat(testng.getStatus()).isEqualTo(0);
  }
}
