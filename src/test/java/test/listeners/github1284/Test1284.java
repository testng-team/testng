package test.listeners.github1284;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Test1284 {
    @Test
    public void test1284Method() {
        Assert.assertNotNull(Listener1284.getInstance());
    }
}
