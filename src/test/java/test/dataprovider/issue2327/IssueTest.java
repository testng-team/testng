package test.dataprovider.issue2327;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2327")
  public void runTest() {
    TestNG testng = create(SampleTestCase.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();

    assertThat(tla.getSkippedTests().size()).isEqualTo(2);

    for (ITestResult skippedTest : tla.getSkippedTests()) {
      assertThat(skippedTest.getParameters()).isNotEmpty();
    }
  }
}
