package test.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;
import test.configuration.issue1035.InvocationTracker;
import test.configuration.issue1035.MyFactory;
import test.configuration.issue3000.TestClassSample;

public class BeforeClassTest extends SimpleBaseTest {

  @Test
  public void beforeClassMethodsShouldRunInParallel() {
    TestNG tng = create(BeforeClassThreadA.class, BeforeClassThreadB.class);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    tng.run();
    assertThat(Math.abs(BeforeClassThreadA.WHEN - BeforeClassThreadB.WHEN)).isLessThan(1000);
  }

  @Test
  public void afterClassShouldRunEvenWithDisabledMethods() {
    TestNG tng = create(ConfigurationDisabledSampleTest.class);
    assertThat(ConfigurationDisabledSampleTest.m_afterWasRun).isFalse();
    tng.run();
    assertThat(ConfigurationDisabledSampleTest.m_afterWasRun).isTrue();
  }

  @Test(description = "GITHUB-1035")
  public void ensureBeforeClassGetsCalledConcurrentlyWhenWorkingWithFactories() {
    TestNG testng = create(MyFactory.class);
    testng.setParallel(ParallelMode.INSTANCES);
    testng.setGroupByInstances(true);
    testng.run();
    List<InvocationTracker> sorted = new ArrayList<>(MyFactory.TRACKER);
    assertThat(sorted).hasSize(5);
    long previousTimestamp = sorted.get(0).getTime();
    long previousThreadId = sorted.get(0).getThreadId();
    for (int i = 1; i < sorted.size(); i++) {
      InvocationTracker current = sorted.get(i);
      assertThat(current.getTime() - previousTimestamp).isLessThanOrEqualTo(100);
      previousTimestamp = current.getTime();
      assertThat(current.getThreadId()).isNotEqualTo(previousThreadId);
      previousThreadId = current.getThreadId();
    }
  }

  @Test(description = "GITHUB-3000")
  public void ensureIndependentConfigurationsAlwaysRunFirstWhenUsingDependencies() {
    TestNG testng = create(TestClassSample.class);
    testng.setVerbose(2);

    List<ITestResult> failures = new ArrayList<>();
    testng.addListener(
        new IReporter() {
          @Override
          public void generateReport(
              List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
            List<ITestResult> filtered =
                suites.stream()
                    .flatMap(it -> it.getResults().values().stream())
                    .flatMap(
                        it ->
                            it.getTestContext().getFailedConfigurations().getAllResults().stream())
                    .collect(Collectors.toList());
            failures.addAll(filtered);
          }
        });
    testng.run();
    assertThat(testng.getStatus()).isZero();
    assertThat(failures).isEmpty();
  }
}
