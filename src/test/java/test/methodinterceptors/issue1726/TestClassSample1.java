package test.methodinterceptors.issue1726;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestClassSample1 {

    @Priority(4)
    @Test(description = "Test Multiple Day Absence Request flow", groups = "Test")
    public void Test1() {
        Reporter.log(getClass().getName() + ".Test1", true);
    }

    @Priority(8)
    @Test(description = "Test Single Day Absence Request Flow", groups = {"Regression", "Test"})
    public void Test2() {
        Reporter.log(getClass().getName() + ".Test2", true);
    }
}