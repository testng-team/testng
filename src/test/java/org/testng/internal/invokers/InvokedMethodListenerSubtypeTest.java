package org.testng.internal.invokers;

import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;

@Test
public class InvokedMethodListenerSubtypeTest {

  @Test
  public void testFromListenerUsingSimpleListenerInstance() {
    final IInvokedMethodListener simpleListenerInstance = new SimpleInvokedMethodListenerDummy();

    InvokedMethodListenerSubtype listenerSubtype =
        InvokedMethodListenerSubtype.fromListener(simpleListenerInstance);

    Assert.assertEquals(listenerSubtype, InvokedMethodListenerSubtype.SIMPLE_LISTENER);
  }

  @Test
  public void testFromListenerUsingExtendedListenerInstance() {
    IInvokedMethodListener2 extendedListenerInstance = new ExtendedInvokedMethodListenerDummy();

    InvokedMethodListenerSubtype listenerSubtype =
        InvokedMethodListenerSubtype.fromListener(extendedListenerInstance);

    Assert.assertEquals(listenerSubtype, InvokedMethodListenerSubtype.EXTENDED_LISTENER);
  }

  static class SimpleInvokedMethodListenerDummy implements IInvokedMethodListener {

    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }
  }

  static class ExtendedInvokedMethodListenerDummy implements IInvokedMethodListener2 {

    public void beforeInvocation(IInvokedMethod method, ITestResult testResult,
                                 ITestContext context) {
    }

    public void afterInvocation(IInvokedMethod method, ITestResult testResult,
                                ITestContext context) {
    }

    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }
  }
}
