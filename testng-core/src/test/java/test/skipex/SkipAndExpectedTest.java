package test.skipex;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class SkipAndExpectedTest extends SimpleBaseTest {

  @Test
  public void shouldSkip() {
    TestNG tng = create(SkipAndExpectedSampleTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);
    tng.run();

    assertThat(tla.getPassedTests()).isEmpty();
    assertThat(tla.getSkippedTests()).hasSize(1);
    assertThat(tla.getFailedTests()).isEmpty();
  }
}
