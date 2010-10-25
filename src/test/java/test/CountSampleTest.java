package test;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Use this test to show run/failed/skip result
 * differences between testng-5.12 and testng-5-14
 *
 * @author CA Technologies
 */


public class CountSampleTest {

    @Test(groups = {"functional"})
    public void testInvokedAndSkipped() throws SkipException {
//        System.out.println("Skipping this test after it is invoked.");
        throw new SkipException("This test is skipped after invocation");
    }

    @Test(groups = {"functional"})
    public static void testInvokedAndFailed() {
//        System.out.println("Failing this test after it is invoked.");
        Assert.fail("Failing this test on purpose");
    }

    @Test(groups = {"functional"}, dependsOnMethods = {"testInvokedAndFailed"})
    public static void testWillNotBeInvokedOnlySkipped() {
//        System.out.println("This test will be skipped, " +
//            "but not invoked because its dependsOnMethod fails.");
    }
}
