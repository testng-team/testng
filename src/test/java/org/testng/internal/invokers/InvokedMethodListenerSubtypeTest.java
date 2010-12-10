package org.testng.internal.invokers;

import org.testng.Assert;
import org.testng.IInvokedMethodListener;
import org.testng.IInvokedMethodListener2;
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
}
