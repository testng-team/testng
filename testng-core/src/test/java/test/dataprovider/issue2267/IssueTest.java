package test.dataprovider.issue2267;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2267")
  public void runTest() {
    TestNG testng = create(SampleTestCase.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();
    assertThat(tla.getFailedTests()).size().isEqualTo(1);
    assertThat(tla.getSkippedTests()).size().isEqualTo(1);
  }
}
