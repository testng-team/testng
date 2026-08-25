package test.configurationfailurepolicy.issue2731;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * Pins what "configfailurepolicy" does to a test method for each level of failing configuration.
 *
 * <p>This is a characterization test: it records the behaviour, it does not argue that the
 * behaviour is right. The row that GITHUB-2731 is about is &#64;BeforeTest under CONTINUE, the only
 * combination where the test method still runs. &#64;BeforeClass and &#64;BeforeMethod skip it
 * under either policy -- CONTINUE only narrows which instance or which invocation the failure
 * invalidates, and these samples have a single one of each -- while &#64;BeforeSuite stops the
 * suite regardless of the policy. Whichever way that inconsistency is eventually settled, these
 * expectations are the ones that have to move.
 */
public class IssueTest extends SimpleBaseTest {

  @DataProvider(name = "dp")
  public Object[][] getData() {
    // params - sample, policy, passed, skipped, configuration failures
    return new Object[][] {
      new Object[] {FailedBeforeSuiteSample.class, XmlSuite.FailurePolicy.SKIP, 0, 1, 1},
      new Object[] {FailedBeforeSuiteSample.class, XmlSuite.FailurePolicy.CONTINUE, 0, 1, 1},
      new Object[] {FailedBeforeTestSample.class, XmlSuite.FailurePolicy.SKIP, 0, 1, 1},
      // GITHUB-2731: the odd one out, the test method runs despite its @BeforeTest having failed
      new Object[] {FailedBeforeTestSample.class, XmlSuite.FailurePolicy.CONTINUE, 1, 0, 1},
      new Object[] {FailedBeforeClassSample.class, XmlSuite.FailurePolicy.SKIP, 0, 1, 1},
      new Object[] {FailedBeforeClassSample.class, XmlSuite.FailurePolicy.CONTINUE, 0, 1, 1},
      new Object[] {FailedBeforeMethodSample.class, XmlSuite.FailurePolicy.SKIP, 0, 1, 1},
      new Object[] {FailedBeforeMethodSample.class, XmlSuite.FailurePolicy.CONTINUE, 0, 1, 1},
    };
  }

  @Test(dataProvider = "dp", description = "GITHUB-2731")
  public void configFailureDecidesTheFateOfTheTestMethod(
      Class<?> sample,
      XmlSuite.FailurePolicy policy,
      int passedTests,
      int skippedTests,
      int configurationFailures) {

    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create(sample);
    testng.addListener(tla);
    testng.setConfigFailurePolicy(policy);
    testng.run();

    String context = sample.getSimpleName() + " with configfailurepolicy=" + policy;
    assertThat(tla.getPassedTests()).describedAs("passed tests, " + context).hasSize(passedTests);
    assertThat(tla.getSkippedTests())
        .describedAs("skipped tests, " + context)
        .hasSize(skippedTests);
    assertThat(tla.getFailedTests()).describedAs("failed tests, " + context).isEmpty();
    // The failure is always reported, so "continue" never turns a broken setup into a green run.
    assertThat(tla.getConfigurationFailures())
        .describedAs("configuration failures, " + context)
        .hasSize(configurationFailures);
  }
}
