package test.listeners.github1130;

import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHub1130Test extends SimpleBaseTest {

    @Test(description = "GITHUB-1130: TestListener should only be instantiated once")
    public void classListenerShouldBeOnlyInstanciatedOnce() {
        MyListener.beforeSuiteCount = 0;
        MyListener.beforeClassCount = 0;
        TestNG tng = createTests("GITHUB-1130", ASample.class, BSample.class);
        TestListenerAdapter adapter = new TestListenerAdapter();
        tng.addListener((ITestNGListener) adapter);
        tng.run();
        assertThat(adapter.getFailedTests()).isEmpty();
        assertThat(adapter.getSkippedTests()).isEmpty();
        assertThat(MyListener.beforeSuiteCount).isEqualTo(1);
        assertThat(MyListener.beforeClassCount).isEqualTo(2);
    }
}
