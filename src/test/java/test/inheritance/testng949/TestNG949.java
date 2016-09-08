package test.inheritance.testng949;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;

import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class TestNG949 extends SimpleBaseTest {

    @Test
    public void test_depends_on_with_always_run_true_inheritance() {
        TestNG tng = create(DependsOnWithAlwaysRunTrueSample.class);
        InvokedMethodNameListener listener = new InvokedMethodNameListener();
        tng.addListener(listener);

        tng.run();
        assertThat(listener.getSucceedMethodNames()).containsExactly("testMethodForOverride", "testNotOverriddenMethodAlwaysRunTrue");
        assertThat(listener.getFailedMethodNames()).isEmpty();
        assertThat(listener.getSkippedMethodNames()).isEmpty();
    }

    @Test
    public void test_depends_on_with_always_run_false_inheritance() {
        TestNG tng = create(DependsOnWithAlwaysRunFalseSample.class);
        InvokedMethodNameListener listener = new InvokedMethodNameListener();
        tng.addListener(listener);

        tng.run();
        assertThat(listener.getSucceedMethodNames()).containsExactly("testMethodForOverride", "testNotOverriddenMethodAlwaysRunFalse");
        assertThat(listener.getFailedMethodNames()).isEmpty();
        assertThat(listener.getSkippedMethodNames()).isEmpty();
    }
}
