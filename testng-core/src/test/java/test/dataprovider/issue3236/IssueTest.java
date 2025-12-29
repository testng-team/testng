package test.dataprovider.issue3236;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void verifyDataProviderIsReExecuted() {
    TestNG tng = create(SampleTestCase.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    assertThat(tla.getFailedTests()).isEmpty();
    assertThat(tla.getPassedTests()).hasSize(1);
    assertThat(tla.getSkippedTests()).hasSize(1);
  }
}