package test.inheritance.testng949;

import org.testng.annotations.Test;

public class DependsOnWithAlwaysRunFalseBaseSample {

    @Test(dependsOnMethods = "testMethodForOverride", alwaysRun = false)
    public void testNotOverriddenMethodAlwaysRunFalse() {
        System.out.println("DependsOnWithAlwaysRunFalseBaseSample.testNotOverriddenMethodAlwaysRunFalse");
    }

    @Test
    public void testMethodForOverride() {
        System.out.println("DependsOnWithAlwaysRunFalseBaseSample.testMethodForOverride");
    }

}
