package test_result;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test_result.issue1590.TestclassSample;
import test_result.issue2535.CalculatorTestSample;
import test_result.issue2535.CalculatorTestSample.LocalListener;

public class TestResultTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1590")
  public void ensureITestResultHasValidStatusAndTimestampWhenInvokingConfigurationMethod() {
    TestNG testng = create(TestclassSample.class);
    testng.run();
    assertThat(TestclassSample.startTimestamp).isLessThanOrEqualTo(0);
    assertThat(TestclassSample.startStatus).isEqualTo(ITestResult.STARTED);
    assertThat(TestclassSample.endTimestamp).isGreaterThan(0);
    assertThat(TestclassSample.endStatus).isEqualTo(ITestResult.SUCCESS);
  }

  @Test(description = "GITHUB-2535")
  public void ensureITestResultHasValidTimestampWhenConfigurationMethodFails() {
    TestNG testng = create(CalculatorTestSample.class);
    testng.run();
    Map<String, ITestResult> results = LocalListener.results;
    ITestResult skippedConfig = results.get("setup1");
    assertThat(skippedConfig.getEndMillis()).isEqualTo(skippedConfig.getStartMillis());
  }
}
