package org.testng.methodinterceptors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.factory.samples.lazy.CountingFactorySample;
import org.testng.internal.RuntimeBehavior;
import org.testng.methodinterceptors.samples.CycleSample;
import org.testng.methodinterceptors.samples.DependsOnGroupsSample;
import org.testng.methodinterceptors.samples.DependsOnMethodsSample;
import org.testng.methodinterceptors.samples.FirstClassSample;
import org.testng.methodinterceptors.samples.MissingGroupSample;
import org.testng.methodinterceptors.samples.RecordingInterceptor;
import org.testng.methodinterceptors.samples.SecondClassSample;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

/**
 * An {@link IMethodInterceptor} is given every test method of its {@code <test>}, the ones taking
 * part in a dependency included -- which is what GITHUB-1263 reported against a javadoc promising
 * the opposite. Rather than withhold them, which would make them impossible to remove, TestNG tells
 * the interceptor what each method it is given must run after and what must run after it. Every
 * sentence the {@code IMethodInterceptor} javadoc makes of that is pinned here.
 */
public class Issue1263Test extends SimpleBaseTest {

  @DataProvider(name = "dependencySamples")
  public Object[][] dependencySamples() {
    return new Object[][] {{DependsOnMethodsSample.class}, {DependsOnGroupsSample.class}};
  }

  @Test(description = "GITHUB-1263", dataProvider = "dependencySamples")
  public void aDeclaredDependencyIsVisibleToAnInterceptor(Class<?> sample) {
    RecordingInterceptor interceptor = record(sample);

    assertThat(interceptor.received())
        .containsExactlyInAnyOrder("independent", "prerequisite", "dependent");
    assertThat(interceptor.upstreamOf("independent")).isEmpty();
    assertThat(interceptor.downstreamOf("independent")).isEmpty();
    assertThat(interceptor.upstreamOf("prerequisite")).isEmpty();
    assertThat(interceptor.downstreamOf("prerequisite")).containsExactly("dependent");
    assertThat(interceptor.upstreamOf("dependent")).containsExactly("prerequisite");
    assertThat(interceptor.downstreamOf("dependent")).isEmpty();
  }

  @DataProvider(name = "derivedOrders")
  public Object[][] derivedOrders() {
    return new Object[][] {{true, false}, {false, true}};
  }

  @Test(
      description = "GITHUB-1263: only a declared dependency is reported, never a derived order",
      dataProvider = "derivedOrders")
  public void anOrderTestNGDerivedItselfIsNotReported(
      boolean preserveOrder, boolean groupByInstances) {
    XmlSuite suite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(suite, "test", FirstClassSample.class, SecondClassSample.class);
    xmlTest.setPreserveOrder(preserveOrder);
    xmlTest.setGroupByInstances(groupByInstances);

    RecordingInterceptor interceptor = new RecordingInterceptor();
    TestNG tng = create(suite);
    tng.addListener((ITestNGListener) interceptor);
    tng.run();

    // Both orders do put the second class after the first, and neither is a declared dependency,
    // so neither is the interceptor's to act on.
    assertThat(interceptor.received()).containsExactlyInAnyOrder("first", "second");
    assertThat(interceptor.upstreamOf("second")).isEmpty();
    assertThat(interceptor.downstreamOf("first")).isEmpty();
  }

  @Test(description = "GITHUB-1263: what the javadoc warns a removal costs")
  public void removingAMethodThatOthersDeclareADependencyUponEndsTheRun() {
    assertThatThrownBy(() -> runWithout("prerequisite", DependsOnMethodsSample.class))
        .isInstanceOf(TestNGException.class)
        .hasMessageContaining("dependent() is depending on method")
        .hasMessageContaining("prerequisite(), which is not annotated with @Test or not included");
  }

  @Test(description = "GITHUB-1263: removing the dependent end of the pair is fine")
  public void removingADependentMethodLeavesTheRestOfTheRunAlone() {
    assertThat(runWithout("dependent", DependsOnMethodsSample.class))
        .containsExactlyInAnyOrder("independent", "prerequisite");
  }

  @Test(description = "GITHUB-1263: an order TestNG derived itself is safe to drop")
  public void removingAMethodOnlyPreserveOrderBindsIsFine() {
    assertThat(runWithout("first", FirstClassSample.class, SecondClassSample.class))
        .containsExactly("second");
  }

  @DataProvider(name = "parallelModes")
  public Object[][] parallelModes() {
    return new Object[][] {
      {XmlSuite.ParallelMode.NONE},
      {XmlSuite.ParallelMode.METHODS},
      {XmlSuite.ParallelMode.CLASSES},
      {XmlSuite.ParallelMode.INSTANCES}
    };
  }

  @Test(
      description = "GITHUB-1263: what is reported does not depend on the parallel mode",
      dataProvider = "parallelModes")
  public void aDeclaredDependencyIsReportedWhateverTheParallelMode(XmlSuite.ParallelMode mode) {
    XmlSuite suite = createXmlSuite("suite");
    XmlTest xmlTest = createXmlTest(suite, "test", DependsOnMethodsSample.class);
    xmlTest.setParallel(mode);

    RecordingInterceptor interceptor = new RecordingInterceptor();
    TestNG tng = create(suite);
    tng.addListener((ITestNGListener) interceptor);
    tng.run();

    assertThat(interceptor.upstreamOf("dependent")).containsExactly("prerequisite");
    assertThat(interceptor.downstreamOf("prerequisite")).containsExactly("dependent");
    assertThat(interceptor.upstreamOf("independent")).isEmpty();
  }

  @Test(description = "GITHUB-1263: an unresolvable group is the run's to reject, not this pass's")
  public void anInterceptorCanStillDropAMethodWhoseGroupHoldsNothing() {
    assertThat(runWithout("heavy", MissingGroupSample.class)).containsExactly("light");
  }

  @Test(description = "GITHUB-1263: an interceptor can still resolve a cycle by dropping")
  public void anInterceptorCanStillBreakADependencyCycle() {
    assertThat(runWithout("two", CycleSample.class)).containsExactlyInAnyOrder("one", "twoPrime");
  }

  @Test(description = "GITHUB-1263: reading the relation must not build a lazy @Factory instance")
  public void readingTheRelationLeavesLazyInstancesAlone() {
    System.setProperty(RuntimeBehavior.MEMORY_FRIENDLY_MODE, "true");
    try {
      CountingFactorySample.reset();
      TestNG tng = create(FirstClassSample.class, CountingFactorySample.class);
      tng.setPreserveOrder(true);
      tng.setLazyFactoryInstantiation(true);
      AtomicInteger constructedWhenIntercepted = new AtomicInteger(-1);
      tng.addListener(
          (ITestNGListener)
              (IMethodInterceptor)
                  (methods, context) -> {
                    methods.forEach(each -> each.getMethod().getMethodName());
                    constructedWhenIntercepted.set(CountingFactorySample.CONSTRUCTED.get());
                    return methods;
                  });
      tng.run();

      assertThat(constructedWhenIntercepted).hasValue(0);
    } finally {
      System.clearProperty(RuntimeBehavior.MEMORY_FRIENDLY_MODE);
    }
  }

  @Test(description = "GITHUB-1263: the two halves together -- read the relation, then drop on it")
  public void anInterceptorCanDropOnTheRelationItReads() {
    TestNG tng = create(DependsOnMethodsSample.class);
    // Drops everything taking part in a dependency, which is what the relation is there for.
    tng.addListener(
        (ITestNGListener)
            (IMethodInterceptor)
                (methods, context) ->
                    methods.stream()
                        .filter(
                            each ->
                                each.getMethod().upstreamDependencies().isEmpty()
                                    && each.getMethod().downstreamDependencies().isEmpty())
                        .collect(Collectors.toList()));

    assertThat(run(false, tng).getInvokedMethodNames()).containsExactly("independent");
  }

  private static RecordingInterceptor record(Class<?>... samples) {
    RecordingInterceptor interceptor = new RecordingInterceptor();
    TestNG tng = create(samples);
    tng.addListener((ITestNGListener) interceptor);
    tng.run();
    return interceptor;
  }

  private static List<String> runWithout(String methodName, Class<?>... samples) {
    TestNG tng = create(samples);
    tng.addListener((ITestNGListener) new RemovingInterceptor(methodName));
    return run(false, tng).getInvokedMethodNames();
  }

  /** Returns the methods in the reverse of the order it was given them. */
  private static class ReversingInterceptor implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      List<IMethodInstance> reversed = new ArrayList<>(methods);
      Collections.reverse(reversed);
      return reversed;
    }
  }

  private static class RemovingInterceptor implements IMethodInterceptor {

    private final String methodName;

    RemovingInterceptor(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
      return methods.stream()
          .filter(each -> !each.getMethod().getMethodName().equals(methodName))
          .collect(Collectors.toList());
    }
  }
}
