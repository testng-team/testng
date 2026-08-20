package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
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

    System.setProperty("testng.memory.friendly", "true");
    try {
      assertThat(TestResult.newTestResult(method, new Object[0], 0).getMethod()).isSameAs(method);
    } finally {
      System.clearProperty("testng.memory.friendly");
    }
  }

  @Test
  public void theCarrierIsNamedFromTheMomentItIsBuilt() {
    ITestNGMethod method = methodBoundToAClass();

    TestResult carrier = TestResult.newTestResult(method, new Object[0], 0);

    assertThat(carrier.getName()).isEqualTo("testMethod");
    assertThat(carrier.getInstanceName()).isEqualTo("testClass");
    // The carrier still knows nothing about the outcome.
    assertThat(carrier.getStatus()).isEqualTo(org.testng.ITestResult.CREATED);
    assertThat(carrier.getStartMillis()).isZero();
    assertThat(carrier.getEndMillis()).isZero();
    assertThat(carrier.getTestContext()).isNull();
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
