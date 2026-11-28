package org.testng.conffailure.issue3627;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.issue3627.EqualInstancesSample;
import org.testng.conffailure.samples.issue3627.FailingAndSkippingSetupsSample;
import org.testng.conffailure.samples.issue3627.FailingSetupsSample;
import org.testng.xml.XmlSuite.FailurePolicy;
import test.SimpleBaseTest;

/**
 * A test that a configuration failure or skip of its own instance skips gets the exception of that
 * instance. Another {@code @Factory} instance that only equals the own instance must not give its
 * exception instead.
 */
public class Issue3627Test extends SimpleBaseTest {

  @DataProvider
  public static Object[][] lazyFactory() {
    return new Object[][] {{false}, {true}};
  }

  @Test(description = "GITHUB-3627", dataProvider = "lazyFactory")
  public void eachEqualInstanceGetsItsOwnConfigurationFailure(boolean lazy) {
    Map<String, Throwable> throwables =
        skippedTestThrowables(FailingSetupsSample.class, FailurePolicy.CONTINUE, lazy);

    assertThat(throwables).containsOnlyKeys("first", "second", "third", "fourth", "fifth");
    throwables.forEach(
        (name, throwable) ->
            assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(name + " setup failed"));
  }

  @Test(description = "GITHUB-3627", dataProvider = "lazyFactory")
  public void aFailureOfAnEqualInstanceDoesNotHideTheOwnConfigurationSkip(boolean lazy) {
    Map<String, Throwable> throwables =
        skippedTestThrowables(FailingAndSkippingSetupsSample.class, FailurePolicy.CONTINUE, lazy);

    assertThat(throwables).containsOnlyKeys("failing", "skipping");
    assertThat(throwables.get("failing"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("failing setup failed");
    assertThat(throwables.get("skipping"))
        .isInstanceOf(SkipException.class)
        .hasMessage("skipping setup skipped");
  }

  // With the skip policy, the failure of the instance that runs first skips the setup of the other
  // ones. That failure is then the reason for each skip, so every instance gets it.
  @Test(description = "GITHUB-3627", dataProvider = "lazyFactory")
  public void withTheSkipPolicyEveryInstanceGetsTheOneFailure(boolean lazy) {
    TestListenerAdapter tla = run(FailingSetupsSample.class, FailurePolicy.SKIP, lazy);

    assertThat(tla.getConfigurationFailures()).hasSize(1);
    Throwable failure = tla.getConfigurationFailures().get(0).getThrowable();
    assertThat(skippedTestThrowables(tla))
        .containsOnlyKeys("first", "second", "third", "fourth", "fifth")
        .allSatisfy((name, throwable) -> assertThat(throwable).isSameAs(failure));
  }

  private static Map<String, Throwable> skippedTestThrowables(
      Class<?> testClass, FailurePolicy policy, boolean lazy) {
    return skippedTestThrowables(run(testClass, policy, lazy));
  }

  private static TestListenerAdapter run(Class<?> testClass, FailurePolicy policy, boolean lazy) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create(testClass);
    testng.setConfigFailurePolicy(policy);
    testng.setLazyFactoryInstantiation(lazy);
    testng.addListener(tla);
    testng.run();
    return tla;
  }

  private static Map<String, Throwable> skippedTestThrowables(TestListenerAdapter tla) {
    // Not Collectors.toMap: it rejects a null throwable, and the assertions should report one.
    Map<String, Throwable> throwables = new HashMap<>();
    for (ITestResult result : tla.getSkippedTests()) {
      throwables.put(instanceName(result), result.getThrowable());
    }
    return throwables;
  }

  private static String instanceName(ITestResult result) {
    return ((EqualInstancesSample) result.getInstance()).getName();
  }
}
