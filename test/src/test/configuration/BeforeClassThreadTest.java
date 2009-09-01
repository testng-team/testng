package test.configuration;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import junit.framework.Assert;

public class BeforeClassThreadTest extends SimpleBaseTest{

    @Test
    public void beforeClassMethodsShouldRunInParallel() {
        TestNG tng = create(new Class[] { BeforeClassThreadA.class, BeforeClassThreadB.class });
        tng.setParallel("methods");
        tng.run();

        Assert.assertTrue(Math.abs(BeforeClassThreadA.WHEN - BeforeClassThreadB.WHEN) < 1000);
    }
}
