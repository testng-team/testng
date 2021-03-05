package org.testng.internal.issue2426;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestClass {
    @DataProvider (name = "constructorArguments")
    public static Object[][] constructorArguments() {
        return new Object[][] { { 1, false }, { 2, true }, { 3, false }, { 4, true }, };
    }

    @Factory (dataProvider = "constructorArguments")
    public TestClass(int number, final boolean bool) {
        System.out.println("Params: " + number + " " + bool);
    }

    @BeforeClass
    public void setup() {
        System.out.println("Before Class");
    }

    @Test
    public void test() {
        System.out.println("Finished");
    }
}
