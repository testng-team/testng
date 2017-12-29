package test.dependent.issue1648;

import org.testng.annotations.Test;

public class ClassBSample extends ClassASample {
    @Test(dependsOnMethods = {"test2"})
    protected void test3() {
        addLog("B test 1");
    }

    @Test(dependsOnMethods = {"test3"})
    protected void test4() {
        addLog("B test 2");
    }
}
