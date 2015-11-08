package test.ignore;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class IgnoreTest extends SimpleBaseTest {

    @Test
    public void ignore_class_should_not_run_tests() {
        TestNG tng = create(IgnoreClassSample.class);
        InvokedMethodListener listener = new InvokedMethodListener();
        tng.addListener(listener);
        tng.run();

        assertThat(listener.getInvokedMethods()).isEmpty();
    }

    @Test
    public void ignore_test_should_not_run_test() {
        TestNG tng = create(IgnoreTestSample.class);
        InvokedMethodListener listener = new InvokedMethodListener();
        tng.addListener(listener);
        tng.run();

        assertThat(listener.getInvokedMethods()).containsExactly(
                "test"
        );
    }
}
