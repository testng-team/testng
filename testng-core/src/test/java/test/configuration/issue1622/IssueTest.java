package test.configuration.issue1622;

import static org.assertj.core.api.Assertions.assertThat;

import org.jspecify.annotations.Nullable;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.TestHelper;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1622")
  public void alwaysRunBeforeConfigurationsAreSkippedAfterAFailedBeforeSuite() {
    TestListenerAdapter tla = runSampleWith(null);

    assertOnlyTheAfterConfigurationsRan(tla);
  }

  @Test(description = "GITHUB-1622")
  public void alwaysRunBeforeConfigurationsAreSkippedWhateverTheConfigFailurePolicy() {
    TestListenerAdapter tla = runSampleWith(XmlSuite.FailurePolicy.CONTINUE);

    // Same expectation as with the default SKIP policy: what alwaysRun means on a @Before method
    // does not depend on the configuration failure policy.
    assertOnlyTheAfterConfigurationsRan(tla);
  }

  private static TestListenerAdapter runSampleWith(XmlSuite.@Nullable FailurePolicy policy) {
    FailingBeforeSuiteSample.LOGS.clear();
    TestNG tng = create(FailingBeforeSuiteSample.class);
    // A null policy leaves the suite with the default one, which is SKIP.
    tng.setConfigFailurePolicy(policy);
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);
    tng.run();
    return tla;
  }

  private static void assertOnlyTheAfterConfigurationsRan(TestListenerAdapter tla) {
    // The @BeforeSuite failed, so the alwaysRun @BeforeTest, @BeforeClass and @BeforeMethod are
    // skipped -- alwaysRun only lifts the group filtering for them. The alwaysRun @After methods
    // still run, which is what alwaysRun is documented to mean for them.
    assertThat(FailingBeforeSuiteSample.LOGS)
        .containsExactly(
            "failingBeforeSuite", "afterMethod", "afterClass", "afterTest", "afterSuite");
    TestHelper.assertCounts(tla, 1, 3, 1);
  }
}
