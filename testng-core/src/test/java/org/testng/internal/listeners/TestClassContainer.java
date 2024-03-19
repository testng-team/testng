package org.testng.internal.listeners;

import org.testng.annotations.Listeners;

public class TestClassContainer {
  @Listeners({
    ListenerFactoryContainer.DummyListenerFactory.class,
    ListenerFactoryContainer.DummyListenerFactory.class
  })
  public static class TestClassWithDuplicateListenerFactories {}

  @Listeners({
    ListenerFactoryContainer.DummyListenerFactory.class,
    ListenerFactoryContainer.DummyListenerFactory2.class
  })
  public static class TestClassWithMultipleUniqueListenerFactories {}
}
