package org.testng.conffailure.issue1622;

import static org.assertj.core.api.Assertions.assertThat;

import org.jspecify.annotations.Nullable;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.OutcomeRecorder;
import org.testng.conffailure.samples.issue1622.BothRolesSample;
import org.testng.conffailure.samples.issue1622.FailingBeforeClassSample;
import org.testng.conffailure.samples.issue1622.FailingBeforeSuiteSample;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.TestHelper;

public class IssueTest extends SimpleBaseTest {

  @DataProvider
  public static Object[][] policies() {
    // null leaves the suite with the default policy, which is SKIP.
    return new Object[][] {{null}, {XmlSuite.FailurePolicy.CONTINUE}};
  }

  @Test(description = "GITHUB-1622", dataProvider = "policies")
  public void alwaysRunBeforeConfigurationsAreSkippedAfterAFailedBeforeSuite(
      XmlSuite.@Nullable FailurePolicy policy) {
    FailingBeforeSuiteSample.LOGS.clear();
    TestNG tng = create(FailingBeforeSuiteSample.class);
    tng.setConfigFailurePolicy(policy);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();

    // The @BeforeSuite failed, so the alwaysRun @BeforeTest, @BeforeClass and @BeforeMethod are
    // skipped -- alwaysRun only lifts the group filtering for them. The alwaysRun @After methods
    // still run, which is what alwaysRun is documented to mean for them. A suite failure is
    // reported before the policy is read, so both policies answer the same.
    assertThat(FailingBeforeSuiteSample.LOGS)
        .containsExactly(
            "failingBeforeSuite", "afterMethod", "afterClass", "afterTest", "afterSuite");
    TestHelper.assertCounts(tla, 1, 3, 1);
  }

  @Test(description = "GITHUB-1622", dataProvider = "policies")
  public void alwaysRunBeforeMethodIsSkippedAfterAFailedBeforeClass(
      XmlSuite.@Nullable FailurePolicy policy) {
    // A class failure is the level the policy does distinguish: under CONTINUE the failure is
    // recorded for the instance, under SKIP for the class. The alwaysRun @BeforeMethod is skipped
    // either way.
    assertThat(outcomesOf(policy, FailingBeforeClassSample.class))
        .containsExactly(
            "CONFIG FAIL failingBeforeClass",
            "CONFIG SKIP beforeMethod",
            "TEST SKIP testMethod",
            "CONFIG PASS afterMethod");
  }

  @Test(description = "GITHUB-1622")
  public void alwaysRunOnTheAfterRoleDoesNotBypassInTheBeforeRole() {
    // @BeforeMethod @AfterMethod(alwaysRun = true) on one method: skipped as a setup, run as a
    // teardown. TestNG merges the two annotations, so the role has to come from the method.
    assertThat(outcomesOf(null, BothRolesSample.class))
        .containsExactly(
            "CONFIG FAIL failingBeforeClass",
            "CONFIG SKIP both",
            "TEST SKIP testMethod",
            "CONFIG PASS both");
  }

  private static java.util.List<String> outcomesOf(
      XmlSuite.@Nullable FailurePolicy policy, Class<?> sample) {
    TestNG tng = create(sample);
    tng.setConfigFailurePolicy(policy);
    OutcomeRecorder recorder = new OutcomeRecorder();
    tng.addListener(recorder);
    tng.run();
    return recorder.getOutcomes();
  }
}
