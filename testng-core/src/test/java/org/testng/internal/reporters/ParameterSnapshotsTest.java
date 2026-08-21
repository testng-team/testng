package org.testng.internal.reporters;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

/**
 * The store and what it holds, on their own, with results built by hand: no TestNG run, so nothing
 * but this test ever touches the values it counts renderings of.
 */
public class ParameterSnapshotsTest {

  @Test(description = "Nothing is rendered until a reporter says it will read the snapshots")
  public void nothingIsCapturedUntilItIsRequested() {
    CountingParameter parameter = new CountingParameter("value");
    ParameterSnapshots snapshots = new ParameterSnapshots();

    ITestResult result = resultOf(parameter);
    snapshots.captureIfAbsent(result);

    assertThat(parameter.renderings()).isZero();
    assertThat(snapshots.find(result)).isNull();
  }

  @Test(description = "The reporting representation of an invocation is rendered at most once")
  public void repeatedCapturesRenderTheValueOnce() {
    CountingParameter parameter = new CountingParameter("value");
    ITestResult result = resultOf(parameter);
    ParameterSnapshots snapshots = requestedSnapshots();

    snapshots.captureIfAbsent(result);
    snapshots.captureIfAbsent(result);
    snapshots.captureIfAbsent(result);

    assertThat(parameter.renderings()).isEqualTo(1);
    assertThat(requireNonNull(snapshots.find(result)).renderedValues()).containsExactly("value");
  }

  @Test(
      description = "Two results that compare equal keep the values their own invocation ran with")
  public void snapshotsAreKeyedByResultIdentity() {
    ITestResult first = indistinguishable(resultOf(new CountingParameter("first")));
    ITestResult second = indistinguishable(resultOf(new CountingParameter("second")));
    // The premise: ITestResult is a public interface, and an implementation is free to do this.
    assertThat(first).isEqualTo(second);
    ParameterSnapshots snapshots = requestedSnapshots();

    snapshots.captureIfAbsent(first);
    snapshots.captureIfAbsent(second);

    assertThat(requireNonNull(snapshots.find(first)).renderedValues()).containsExactly("first");
    assertThat(requireNonNull(snapshots.find(second)).renderedValues()).containsExactly("second");
  }

  @Test(description = "A toString that throws leaves the result unsnapshotted, it does not blow up")
  public void aRenderingThatThrowsIsNotAnError() {
    ITestResult result = resultOf(new ThrowingParameter());
    ParameterSnapshots snapshots = requestedSnapshots();

    snapshots.captureIfAbsent(result);

    assertThat(snapshots.find(result)).isNull();
  }

  @Test(description = "A result a reporter will never print can be dropped before the run ends")
  public void discardedResultsAreForgotten() {
    ITestResult result = resultOf(new CountingParameter("value"));
    ParameterSnapshots snapshots = requestedSnapshots();
    snapshots.captureIfAbsent(result);

    snapshots.discard(result);

    assertThat(snapshots.find(result)).isNull();
  }

  @Test(
      description =
          "A data provider that supplied the wrong number of values leaves the counts to report"
              + " instead of the values")
  public void aCountMismatchIsReportedInsteadOfTheValues() {
    CountingParameter parameter = new CountingParameter("value");

    ParameterSnapshot snapshot =
        requireNonNull(
            ParameterSnapshot.of(
                new Object[] {parameter, parameter}, new Class<?>[] {Object.class}));

    assertThat(snapshot.hasCountMismatch()).isTrue();
    assertThat(snapshot.suppliedCount()).isEqualTo(2);
    assertThat(snapshot.expectedCount()).isEqualTo(1);
    assertThat(snapshot.renderedValues()).isEmpty();
    // There is no type to render a value against, so nothing was rendered.
    assertThat(parameter.renderings()).isZero();
  }

  private static ParameterSnapshots requestedSnapshots() {
    ParameterSnapshots snapshots = new ParameterSnapshots();
    snapshots.requestCapture();
    return snapshots;
  }

  /** A result carrying the given values, declared as plain objects so they render as themselves. */
  private static ITestResult resultOf(Object... parameters) {
    Class<?>[] parameterTypes = new Class<?>[parameters.length];
    Arrays.fill(parameterTypes, Object.class);
    ITestNGMethod method = mock(ITestNGMethod.class);
    when(method.getParameterTypes()).thenReturn(parameterTypes);

    TestResult result = TestResult.newEmptyTestResult();
    result.setMethod(method);
    result.setParameters(parameters);
    return result;
  }

  /** The same result, seen through an {@link ITestResult} that says it equals any other. */
  private static ITestResult indistinguishable(ITestResult delegate) {
    return (ITestResult)
        Proxy.newProxyInstance(
            ITestResult.class.getClassLoader(),
            new Class<?>[] {ITestResult.class},
            (proxy, method, args) -> {
              switch (method.getName()) {
                case "equals":
                  return args[0] instanceof ITestResult;
                case "hashCode":
                  return 0;
                default:
                  return method.invoke(delegate, args);
              }
            });
  }

  /** A value that counts how often it has been rendered, and that no clone() can copy. */
  private static final class CountingParameter {

    private final String value;
    private final AtomicInteger renderings = new AtomicInteger();

    CountingParameter(String value) {
      this.value = value;
    }

    int renderings() {
      return renderings.get();
    }

    @Override
    public String toString() {
      renderings.incrementAndGet();
      return value;
    }
  }

  private static final class ThrowingParameter {

    @Override
    public String toString() {
      throw new IllegalStateException("this parameter cannot be rendered");
    }
  }
}
