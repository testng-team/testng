package test.inheritance.testng949;

import org.testng.annotations.Test;

public class DependsOnWithAlwaysRunFalseSample extends DependsOnWithAlwaysRunFalseBaseSample {

    @Override
    @Test
    public void testMethodForOverride() {
        System.out.println("DependsOnWithAlwaysRunFalseSample.testMethodForOverride");
    }
}
