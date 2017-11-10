package org.testng.internal.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(DummyListenerFactory.class)
public class TestClassWithCompositeListener {
    @Test
    public void testMethod() {}
}
