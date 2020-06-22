package test.dataprovider.issue2327;

import static org.assertj.core.api.Assertions.assertThat;

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

    assertThat(tla.getPassedTests().size()).isEqualTo(1);
    assertThat(tla.getSkippedTests().size()).isEqualTo(1);

    final Object[] parameterSkippedMethod = tla.getSkippedTests().get(0).getParameters();
    assertThat(parameterSkippedMethod).isNotNull();
    assertThat(parameterSkippedMethod).isNotEmpty();
    assertThat(parameterSkippedMethod).contains("Dataset1");
    assertThat(parameterSkippedMethod).doesNotContain("Dataset2");

    final Object[] parametersPassedMethod = tla.getPassedTests().get(0).getParameters();
    assertThat(parametersPassedMethod).isNotNull();
    assertThat(parametersPassedMethod).isNotEmpty();
    assertThat(parametersPassedMethod).contains("Dataset2");
    assertThat(parametersPassedMethod).doesNotContain("Dataset1");
  }
}
