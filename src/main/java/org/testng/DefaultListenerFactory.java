package org.testng;

import org.testng.internal.ClassHelper;

public final class DefaultListenerFactory implements ITestNGListenerFactory {
    @Override
    public ITestNGListener createListener(Class<? extends ITestNGListener> listenerClass) {
        return ClassHelper.newInstance(listenerClass);
    }
}
