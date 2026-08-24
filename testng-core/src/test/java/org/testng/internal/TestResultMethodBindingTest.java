package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.ITest;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class TestResultMethodBindingTest {

  /**
   * In memory friendly mode a reported result holds a {@link LiteWeightTestNGMethod} snapshot, but
   * the parameter carrier must hold the live method: a configuration method that declares an {@link
   * org.testng.ITestResult} parameter is handed that carrier and reaches through it to mutate the
   * method -- {@code test.parameters.Issue1061Sample} sets the timeout that way, and the invoker
   * reads it back off the scheduled method. Writing it to a snapshot would lose it in silence.
   */
  @Test
  public void theParameterCarrierHoldsTheLiveMethodInMemoryFriendlyMode() {
    ITestNGMethod method = methodBoundToAClass();

    System.setProperty(RuntimeBehavior.MEMORY_FRIENDLY_MODE, "true");
    try {
      assertThat(TestResult.newTestResult(method, new Object[0], 0).getMethod()).isSameAs(method);
    } finally {
      System.clearProperty(RuntimeBehavior.MEMORY_FRIENDLY_MODE);
    }
  }

  @Test
  public void theCarrierIsNamedFromTheMomentItIsBuilt() {
    ITestNGMethod method = methodBoundToAClass();

    TestResult carrier = TestResult.newTestResult(method, new Object[0], 0);

    assertThat(carrier.getName()).isEqualTo("testMethod");
    assertThat(carrier.getInstanceName()).isEqualTo("testClass");
    // The carrier still knows nothing about the outcome.
    assertThat(carrier.getStatus()).isEqualTo(ITestResult.CREATED);
    assertThat(carrier.getStartMillis()).isZero();
    assertThat(carrier.getEndMillis()).isZero();
    assertThat(carrier.getTestContext()).isNull();
  }

  /** The naming block moved out of init() into the constructor; these are its other branches. */
  @Test
  public void anITestInstanceNamesTheResult() {
    ITestNGMethod method = methodBoundToAClass();
    when(method.getInstance()).thenReturn((ITest) () -> "named by ITest");

    assertThat(TestResult.newTestResultFor(method).getName()).isEqualTo("named by ITest");
  }

  @Test
  public void aBoundTestNameNamesTheResult() {
    ITestNGMethod method = methodBoundToAClass();
    when(method.getInstance()).thenReturn(new Object());
    when(Utils.requireTestClassOf(method).getTestName()).thenReturn("named by @Test");

    assertThat(TestResult.newTestResultFor(method).getName()).isEqualTo("named by @Test");
  }

  @Test
  public void anOverriddenToStringRefinesBothNames() {
    ITestNGMethod method = methodBoundToAClass();
    when(method.getInstance())
        .thenReturn(
            new Object() {
              @Override
              public String toString() {
                return "shard-3";
              }
            });

    TestResult result = TestResult.newTestResultFor(method);

    assertThat(result.getName()).isEqualTo("testMethod on shard-3");
    assertThat(result.getInstanceName()).isEqualTo("shard-3");
  }

  /**
   * Only the last branch renders the instance into the instance name. An {@link ITest} names the
   * result itself, so an overridden toString() on the same object must not reach the instance name.
   */
  @Test
  public void anITestInstanceKeepsTheClassAsItsInstanceName() {
    ITestNGMethod method = methodBoundToAClass();
    when(method.getInstance())
        .thenReturn(
            new ITest() {
              @Override
              public String getTestName() {
                return "named by ITest";
              }

              @Override
              public String toString() {
                return "shard-3";
              }
            });

    TestResult result = TestResult.newTestResultFor(method);

    assertThat(result.getName()).isEqualTo("named by ITest");
    assertThat(result.getInstanceName()).isEqualTo("testClass");
  }

  private static ITestNGMethod methodBoundToAClass() {
    ITestClass testClass = mock(ITestClass.class);
    when(testClass.getName()).thenReturn("testClass");
    ITestNGMethod method = mock(ITestNGMethod.class);
    when(method.getMethodName()).thenReturn("testMethod");
    when(method.getTestClass()).thenReturn(testClass);
    return method;
  }
}
