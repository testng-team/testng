package test.listeners.github2415;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FirstTestClass {
    @Test(description = "First test step")
    public void firstMethod() {
    }

    @Test(description = "Second test step", dependsOnMethods = "firstMethod")
    public void secondMethod() {
        Assert.fail();
    }

    @Test(description = "Third test step", dependsOnMethods = "secondMethod")
    public void thirdMethod() {
    }
}
