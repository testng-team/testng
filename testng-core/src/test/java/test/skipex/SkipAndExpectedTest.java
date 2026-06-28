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

    assertThat(tla.getPassedTests().size()).isEqualTo(0);
    assertThat(tla.getSkippedTests().size()).isEqualTo(1);
    assertThat(tla.getFailedTests().size()).isEqualTo(0);
  }
}
