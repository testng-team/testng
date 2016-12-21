package test.listeners.github1284;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class Test1284C {
    @Test
    public void test1284C() {
        Assert.assertNotNull(Listener1284.getInstance());
        List<String> testList = Listener1284.testList;
        Assert.assertTrue(testList.contains(Test1284C.class.getName() + " - Before Invocation"));
        Assert.assertFalse(testList.contains(Test1284C.class.getName() + " - After Invocation"));
    }
}
