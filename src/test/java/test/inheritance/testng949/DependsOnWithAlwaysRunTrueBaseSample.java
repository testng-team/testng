package test.inheritance.testng949;

import org.testng.annotations.Test;

public class DependsOnWithAlwaysRunTrueBaseSample {

    @Test(dependsOnMethods = "testMethodForOverride", alwaysRun = true)
    public void testNotOverriddenMethodAlwaysRunTrue() {
        System.out.println("DependsOnWithAlwaysRunTrueBaseSample.testNotOverriddenMethodAlwaysRunTrue");
    }

    @Test
    public void testMethodForOverride() {
        System.out.println("DependsOnWithAlwaysRunTrueBaseSample.testMethodForOverride");
    }

}
