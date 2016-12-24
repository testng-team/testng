package test.listeners.github1284;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;

public class Test1284B {
    @Test
    public void testTheOrderOfInvokedMethods() {
        Assert.assertNotNull(Listener1284.getInstance());
        Assert.assertEquals(Listener1284.testList.size(), 5);

        String b1 = Test1284.class.getName() + " - Before Invocation";
        String a1 = Test1284.class.getName() + " - After Invocation";
        String b2 = Test1284B.class.getName() + " - Before Invocation";

        List<String> expectedList = Lists.newArrayList(b1, a1, b1, a1, b2);
        Assert.assertEquals(Listener1284.testList, expectedList);
    }
}
