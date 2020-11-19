package test.listeners.github2415;

import org.testng.annotations.Test;

public class SecondTestClass {
    @Test(description = "First test step")
    public void firstMethod() {
    }

    @Test(description = "Second test step", dependsOnMethods = "firstMethod")
    public void secondMethod() {
    }

    @Test(description = "Third test step", dependsOnMethods = "secondMethod")
    public void thirdMethod() {
    }
}
