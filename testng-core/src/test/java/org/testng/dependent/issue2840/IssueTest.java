package org.testng.dependent.issue2840;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.dependent.samples.issue2840.DownstreamSample;
import org.testng.dependent.samples.issue2840.FactorySample;
import org.testng.dependent.samples.issue2840.LazyFactorySample;
import org.testng.dependent.samples.issue2840.SameNameFailingSample;
import org.testng.dependent.samples.issue2840.SameNameSample;
import org.testng.dependent.samples.issue2840.UpstreamSample;
import test.SimpleBaseTest;

/**
 * GITHUB-2840. A method is skipped when a method of another class that it names in {@code
 * dependsOnMethods} failed.
 */
public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2840")
  public void methodIsSkippedWhenItsDependencyInAnotherClassFailed() {
    TestListenerAdapter results = runClasses(DownstreamSample.class, UpstreamSample.class);
    assertThat(names(results.getPassedTests()))
        .containsExactlyInAnyOrder(
            "UpstreamSample.passes", "DownstreamSample.afterPassingUpstream");
    assertThat(names(results.getFailedTests())).containsExactly("UpstreamSample.fails");
    assertThat(names(results.getSkippedTests()))
        .containsExactly("DownstreamSample.afterFailingUpstream");
  }

  @Test(description = "GITHUB-2840")
  public void methodStillRunsWhenAMethodOfTheSameNameFailedInAnUnrelatedClass() {
    TestListenerAdapter results = runClasses(SameNameFailingSample.class, SameNameSample.class);
    assertThat(names(results.getPassedTests()))
        .containsExactlyInAnyOrder("SameNameSample.first", "SameNameSample.second");
    assertThat(names(results.getFailedTests())).containsExactly("SameNameFailingSample.first");
    assertThat(results.getSkippedTests()).isEmpty();
  }

  @Test(description = "GITHUB-2840")
  public void factoryInstanceStillRunsWhenItsDependencyFailedOnAnotherInstance() {
    TestListenerAdapter results = runClasses(FactorySample.class);
    assertThat(names(results.getPassedTests()))
        .containsExactlyInAnyOrder("FactorySample.first", "FactorySample.second");
    assertThat(names(results.getFailedTests())).containsExactly("FactorySample.first");
    assertThat(names(results.getSkippedTests())).containsExactly("FactorySample.second");
  }

  @Test(description = "GITHUB-2840")
  public void dependencyCheckDoesNotCreateALazyFactoryInstanceEarly() {
    LazyFactorySample.EVENTS.clear();
    TestNG testng = create(LazyFactorySample.class);
    testng.setGroupByInstances(true);
    testng.run();
    assertThat(LazyFactorySample.EVENTS)
        .containsExactly("create 0", "first 0", "second 0", "create 1", "first 1", "second 1");
  }

  private static TestListenerAdapter runClasses(Class<?>... classes) {
    TestNG testng = create(classes);
    TestListenerAdapter results = new TestListenerAdapter();
    testng.addListener(results);
    testng.run();
    return results;
  }

  private static List<String> names(Collection<ITestResult> results) {
    return results.stream()
        .map(
            r ->
                r.getTestClass().getRealClass().getSimpleName()
                    + "."
                    + r.getMethod().getMethodName())
        .collect(Collectors.toList());
  }
}
