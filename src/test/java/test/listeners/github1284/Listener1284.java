package test.listeners.github1284;

import org.testng.IClassListener;
import org.testng.ITestClass;

import java.util.LinkedList;
import java.util.List;

public class Listener1284 implements IClassListener {
    private static Listener1284 instance;
    static List<String> testList = new LinkedList<>();

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
        Listener1284.testList.add(iTestClass.getRealClass().getName() + " - Before Invocation");
    }

    public void onAfterClass(ITestClass iTestClass) {
        Listener1284.testList.add(iTestClass.getRealClass().getName() + " - After Invocation");
    }
}
