package org.testng.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.configuration.samples.BeforeClassParallelSupport;
import org.testng.configuration.samples.ConfigurationDisabledSampleTest;
import org.testng.configuration.samples.ConfigurationOrder;
import org.testng.configuration.samples.issue1035.MyFactory;
import org.testng.configuration.samples.issue3000.TestClassSample;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import test.SimpleBaseTest;

public class BeforeClassTest extends SimpleBaseTest {

  @Test
  public void beforeClassMethodsShouldRunInParallel() {
    BeforeClassParallelSupport.reset();
    TestNG tng = create(BeforeClassParallelSupport.SAMPLES);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    tng.run();

    // Each @BeforeClass waits for the other one, so a sequential run breaks the barrier and fails
    // the configuration. The arrival count guards against a run where nothing executed at all,
    // which would leave the status at zero.
    assertThat(BeforeClassParallelSupport.getArrivals())
        .withFailMessage("every @BeforeClass should have reached the rendezvous")
        .isEqualTo(BeforeClassParallelSupport.SAMPLES.length);
    assertThat(tng.getStatus()).isZero();
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
    MyFactory.reset();
    TestNG testng = create(MyFactory.class);
    testng.setParallel(ParallelMode.INSTANCES);
    testng.setGroupByInstances(true);
    testng.setThreadCount(MyFactory.INSTANCE_COUNT);
    testng.run();

    // The @BeforeClass methods wait for each other, so a sequential execution would time out and
    // fail the configuration. No timing assertion is needed to prove the concurrency.
    assertThat(testng.getStatus()).isZero();
    assertThat(MyFactory.THREAD_IDS)
        .as("each @BeforeClass should have run on its own thread")
        .hasSize(MyFactory.INSTANCE_COUNT)
        .doesNotHaveDuplicates();
  }

  @Test(description = "GITHUB-3000")
  public void ensureIndependentConfigurationsAlwaysRunFirstWhenUsingDependencies()
      throws NoSuchMethodException {
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

    // TestNG finds these methods in hash order, and the hash includes the package name. So the run
    // above can pass without the fix after a rename. Sort the methods from every starting order.
    List<String> methods =
        List.of("setupDependency", "__setupAdditionalDependency_", "beforeClass");
    for (List<String> startingOrder : ConfigurationOrder.allOrdersOf(methods)) {
      assertThat(ConfigurationOrder.sortedFrom(TestClassSample.class, startingOrder))
          .as("@BeforeClass order when TestNG finds the methods as %s", startingOrder)
          .containsExactly("setupDependency", "__setupAdditionalDependency_", "beforeClass");
    }
  }
}
