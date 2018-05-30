package test_result.issue1590;

import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void ensureITestResultHasValidStatusAndTimestampWhenInvokingConfigurationMethod() {
    TestNG testng = create(TestclassSample.class);
    testng.run();
    assertThat(TestclassSample.startTimestamp).isLessThanOrEqualTo(0);
    assertThat(TestclassSample.startStatus).isEqualTo(ITestResult.STARTED);
    assertThat(TestclassSample.endTimestamp).isGreaterThan(0);
    assertThat(TestclassSample.endStatus).isEqualTo(ITestResult.SUCCESS);
  }
}
