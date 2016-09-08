package test.inheritance.testng949;

import org.testng.annotations.Test;

public class DependsOnWithAlwaysRunTrueSample extends DependsOnWithAlwaysRunTrueBaseSample {

    @Override
    @Test
    public void testMethodForOverride() {
        System.out.println("DependsOnWithAlwaysRunTrueSample.testMethodForOverride");
    }
}
