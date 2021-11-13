package test.invocationcount.issue1719;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void testSuccessPercentageCalculation() {
    TestNG testng = create(TestclassSample.class);
    DummyReporter listener = new DummyReporter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getFailures()).isEmpty();
    assertThat(listener.getSkip()).isEmpty();
    assertThat(listener.getSuccess()).isEmpty();
    assertThat(listener.getFailedWithinSuccessPercentage()).hasSize(5);
  }
}
