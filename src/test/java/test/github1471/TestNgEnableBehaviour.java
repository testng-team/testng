package test.github1471;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.enable.InvokedMethodListener;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test test should ensure that disabled tests are not executed.
 * See issue: https://github.com/cbeust/testng/issues/1471
 *
 * @author Kirusanth Poopalasingam ( code@kiru.io )
 */
public class TestNgEnableBehaviour extends SimpleBaseTest{

    @Test
    public void disabled_test() {
        TestNG tng = create(TestA.class, TestDisabled.class);
        InvokedMethodListener listener = new InvokedMethodListener();
        tng.addListener(listener);
        tng.setPreserveOrder(true);
        tng.run();

        assertThat(listener.getInvokedMethods()).containsExactly(
            "testATestMethod"
        );
    }

    @Test(enabled = false)
    public static class TestDisabled {
        @Test
        public void testDisabledTestMethod() {}
    }

    @Test
    public static class TestA {
        @Test
        public void testATestMethod() {}
    }
}
