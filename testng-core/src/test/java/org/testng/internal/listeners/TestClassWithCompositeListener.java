package org.testng.internal.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ListenerFactoryContainer.DummyListenerFactory.class)
public class TestClassWithCompositeListener {
  @Test
  public void testMethod() {}
}
