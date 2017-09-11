package test.listeners.github1465;

import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.testng.Assert.assertTrue;

public class ExampleClassSample {
    @BeforeMethod
    public void beforeMethod(Method method) {
        if ("test1".equals(method.getName())) {
            throw new SkipException("Skip from [before]");
        }
    }

    @Test
    public void test1() {
        assertTrue(true);
    }

    @Test
    public void test2() {
        assertTrue(true);
    }

    @AfterMethod
    public void afterMethod(Method method) {
    }
}
