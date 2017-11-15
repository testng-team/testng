package org.testng.internal.listeners;

import org.testng.IExecutionListener;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;

public class DummyListenerFactory implements ITestNGListenerFactory, IExecutionListener {
    @Override
    public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
        return this;
    }

    @Override
    public void onExecutionStart() {

    }

    @Override
    public void onExecutionFinish() {

    }
}
