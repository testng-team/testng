package org.testng.internal;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.testng.IRetryAnalyzer;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.dynamicgraph.FakeTestClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The collections a method used to allocate up front are now built on first use, and the two that
 * more than one thread can write are installed with a compare-and-set. What that has to guarantee
 * is that the threads racing to create a container all end up writing into the same one: a thread
 * whose container loses the race must not keep writing into it, or what it recorded is dropped when
 * the losing container is.
 *
 * <p>These force that race directly rather than waiting for a run to produce it. A barrier releases
 * every thread on the same instant, which is the only moment the CAS is reachable -- once a
 * container is installed, every later call reads it and there is nothing left to race over.
 */
public class BaseTestMethodTest {

  private static final IAnnotationFinder finder =
      new JDK15AnnotationFinder(new DefaultAnnotationTransformer());

  private static final int THREADS = 8;

  @Test(
      description =
          "Numbers recorded by threads racing to create the failed-invocation queue are all kept")
  public void concurrentlyRecordedFailedInvocationNumbersAreNotLost() throws Exception {
    BaseTestMethod method = newMethod();
    int perThread = 200;

    List<Integer> recorded =
        inParallel(
                thread ->
                    IntStream.range(0, perThread)
                        .map(i -> thread * perThread + i)
                        .peek(method::addFailedInvocationNumber)
                        .boxed()
                        .collect(Collectors.toList()))
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

    assertThat(method.getFailedInvocationNumbers())
        .as("every number handed to a racing thread survives")
        .containsExactlyInAnyOrderElementsOf(recorded);
  }

  @Test(description = "A method that never fails an invocation reports no failed invocations")
  public void aMethodWithNoFailuresReportsAnEmptyListOfInvocationNumbers() {
    assertThat(newMethod().getFailedInvocationNumbers()).isEmpty();
  }

  @Test(
      description =
          "Threads racing to create the retry analyzer cache settle on one analyzer per parameter")
  public void concurrentFirstAccessToTheRetryAnalyzerCacheIsNotDuplicated() throws Exception {
    BaseTestMethod method = newMethod();
    method.setRetryAnalyzerClass(CountingRetryAnalyzer.class);
    ITestResult result = TestResult.newTestResult(method, new Object[] {"a parameter"}, 0);

    Set<IRetryAnalyzer> analyzers = identitySetOf(inParallel(thread -> analyzerOf(method, result)));

    assertThat(analyzers)
        .as("the losing threads read the analyzer the winner installed, rather than their own")
        .hasSize(1);
  }

  @Test(
      description =
          "A retry analyzer created under contention keeps counting for its own parameter alone")
  public void aRetryAnalyzerUnderContentionKeepsItsOwnCount() throws Exception {
    BaseTestMethod method = newMethod();
    method.setRetryAnalyzerClass(CountingRetryAnalyzer.class);
    // One result per thread, each a different parameter of the same method, so every thread asks
    // for a different key of the same not-yet-created map.
    List<ITestResult> results =
        IntStream.range(0, THREADS)
            .mapToObj(i -> TestResult.newTestResult(method, new Object[] {"parameter " + i}, i))
            .collect(Collectors.toList());

    List<IRetryAnalyzer> perThread = inParallel(thread -> analyzerOf(method, results.get(thread)));

    assertThat(identitySetOf(perThread))
        .as("a parameter gets an analyzer of its own, and no parameter's analyzer is dropped")
        .hasSize(THREADS);

    // Asking again returns the cached analyzer rather than a second one, which is what makes a
    // retry count mean anything: a fresh analyzer per call would let a test retry for ever.
    for (int i = 0; i < THREADS; i++) {
      IRetryAnalyzer again = analyzerOf(method, results.get(i));
      assertThat(again).as("the analyzer cached for parameter " + i).isSameAs(perThread.get(i));
      assertThat(((CountingRetryAnalyzer) again).retries()).isZero();
    }
  }

  @Test(description = "A dependency set handed out before a replacement keeps what it held")
  public void replacingTheDependenciesLeavesTheSetsAlreadyHandedOutAlone() {
    BaseTestMethod method = newMethod();
    ITestNGMethod first = newMethod();
    ITestNGMethod second = newMethod();

    method.setUpstreamDependencies(Collections.singleton(first));
    Set<ITestNGMethod> takenBeforeTheReplacement = method.upstreamDependencies();
    method.setUpstreamDependencies(Collections.singleton(second));

    assertThat(takenBeforeTheReplacement)
        .as("a snapshot: the set is replaced, not refilled")
        .containsExactly(first);
    assertThat(method.upstreamDependencies())
        .as("asking again answers the replacement")
        .containsExactly(second);
  }

  @Test(description = "A method with no dependencies answers an empty set both ways")
  public void aMethodWithNoDependenciesAnswersEmptySets() {
    BaseTestMethod method = newMethod();

    assertThat(method.upstreamDependencies()).isEmpty();
    assertThat(method.downstreamDependencies()).isEmpty();

    // Clearing an existing set has to answer empty too, not the set it used to hold.
    method.setDownstreamDependencies(Collections.singleton(newMethod()));
    method.setDownstreamDependencies(Collections.emptySet());
    assertThat(method.downstreamDependencies()).isEmpty();
  }

  /**
   * The analyzer a method hands out for a result. Null only for a method with no retry analyzer
   * set, which none of these are, so a null here is a failure rather than a case to handle.
   */
  private static IRetryAnalyzer analyzerOf(BaseTestMethod method, ITestResult result) {
    return requireNonNull(
        method.getRetryAnalyzer(result), "a method with a retry analyzer class hands one out");
  }

  /**
   * Runs {@code work} on {@link #THREADS} threads released at the same instant, and answers what
   * each one returned. The argument is the thread's own number, which is what a plain {@link
   * Callable} could not carry: two of the callers key the work off it.
   */
  private static <T> List<T> inParallel(IntFunction<T> work) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(THREADS);
    try {
      CyclicBarrier startTogether = new CyclicBarrier(THREADS);
      List<Callable<T>> calls =
          IntStream.range(0, THREADS)
              .mapToObj(
                  thread ->
                      (Callable<T>)
                          () -> {
                            startTogether.await(30, TimeUnit.SECONDS);
                            return work.apply(thread);
                          })
              .collect(Collectors.toList());
      List<T> results = new ArrayList<>();
      // invokeAll waits for every task, so each future below has already finished and the order
      // they are read in costs nothing. The timeout belongs here rather than on get(): the
      // untimed invokeAll waits for ever for a body that hangs, and a timeout on a get() the loop
      // never reaches guards nothing.
      for (Future<T> future : pool.invokeAll(calls, 30, TimeUnit.SECONDS)) {
        // Rethrows whatever the thread threw, so a failure inside one fails the test. A body that
        // ran out of time was cancelled by invokeAll, and shows up here as a CancellationException.
        results.add(future.get());
      }
      return results;
    } finally {
      pool.shutdownNow();
    }
  }

  /** Distinct <em>instances</em>, so that two equal-but-separate analyzers still count as two. */
  private static Set<IRetryAnalyzer> identitySetOf(List<IRetryAnalyzer> analyzers) {
    Set<IRetryAnalyzer> distinct =
        Collections.newSetFromMap(new IdentityHashMap<IRetryAnalyzer, Boolean>());
    distinct.addAll(analyzers);
    return distinct;
  }

  private static BaseTestMethod newMethod() {
    try {
      Method sample = Sample.class.getMethod("aTest");
      TestNGMethod method =
          new TestNGMethod(
              new ITestObjectFactory() {},
              sample,
              finder,
              new XmlTest(new XmlSuite()),
              new IObject.IdentifiableObject(new Sample()));
      // A result carries the class the method belongs to, so the retry tests need one bound.
      method.setTestClass(new FakeTestClass(Sample.class));
      return method;
    } catch (NoSuchMethodException e) {
      throw new AssertionError("the sample method is right here", e);
    }
  }

  /**
   * Counts what it was asked, so that a second analyzer for one parameter shows up as a lost count.
   */
  public static class CountingRetryAnalyzer implements IRetryAnalyzer {

    private final AtomicInteger retries = new AtomicInteger();

    @Override
    public boolean retry(ITestResult result) {
      return retries.incrementAndGet() < 2;
    }

    int retries() {
      return retries.get();
    }
  }

  public static class Sample {
    @Test
    public void aTest() {}
  }
}
