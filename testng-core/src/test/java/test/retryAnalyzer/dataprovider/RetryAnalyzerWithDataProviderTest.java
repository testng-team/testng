package test.retryAnalyzer.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RetryAnalyzerWithDataProviderTest extends SimpleBaseTest {

  @Test
  public void testRetryCounts() {
    TestNG tng = create(RetryCountTest.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener((ITestNGListener) tla);

    tng.run();

    assertThat(tla.getPassedTests()).hasSize(1);
    assertThat(tla.getPassedTests().get(0).getParameters()).isEqualTo(new String[] {"c"});

    assertThat(tla.getFailedTests()).hasSize(3);
    assertThat(tla.getSkippedTests()).hasSize(9);
  }
}
