package test.listeners.github1284;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class Test1284B {
    @Test
    public void testTheOrderOfInvokedMethods() {
        Assert.assertNotNull(Listener1284.getInstance());
        List<String> testList = Listener1284.testList;
        String beforeInvocation = " - Before Invocation";
        String afterInvocation = " - After Invocation";
        String s = Test1284.class.getName() + beforeInvocation;
        Assert.assertTrue(testList.contains(s));
        int test1284BeforeIndex = testList.indexOf(s);
        Assert.assertTrue(testList.size() > 3);
        Assert.assertEquals(Test1284.class.getName() + afterInvocation, testList.get(test1284BeforeIndex + 1));
        Assert.assertEquals(Test1284B.class.getName() + beforeInvocation, testList.get(test1284BeforeIndex + 2));
        Assert.assertFalse(testList.contains(Test1284B.class.getName() + afterInvocation));
    }
}
