package test.listeners;

import org.testng.Assert;
import org.testng.ITestNGListener;

import java.util.List;

public final class ListenerAssert {

    private ListenerAssert() {}

    public static void assertListenerType(List<? extends ITestNGListener> listeners, Class<? extends ITestNGListener> clazz) {
        for (ITestNGListener listener : listeners) {
            if (clazz.isInstance(listener)) {
                return;
            }
        }
        Assert.fail();
    }
}
