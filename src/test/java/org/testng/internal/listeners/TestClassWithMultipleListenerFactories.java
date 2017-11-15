package org.testng.internal.listeners;

import org.testng.annotations.Listeners;

@Listeners({DummyListenerFactory.class, DummyListenerFactory.class})
public class TestClassWithMultipleListenerFactories {
}
