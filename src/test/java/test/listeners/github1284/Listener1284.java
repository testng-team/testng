package test.listeners.github1284;

import org.testng.IClassListener;
import org.testng.ITestClass;

public class Listener1284 implements IClassListener {
    private static Listener1284 instance;

    public Listener1284() {
        setInstance(this);
    }

    private static void setInstance(Listener1284 newInstance) {
        instance = newInstance;
    }

    public static Listener1284 getInstance() {
        return instance;
    }

    public void onBeforeClass(ITestClass iTestClass) {
        // Do nothing
    }

    public void onAfterClass(ITestClass iTestClass) {
        // Do nothing
    }
}
