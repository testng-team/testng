package test_result;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHub1197Test extends SimpleBaseTest {

    @Test
    public void testGitHub1197() {
        TestNG tng = create(GitHub1197Sample.class);

        InvokedMethodNameListener listener = new InvokedMethodNameListener(true);
        tng.addListener((ITestNGListener) listener);

        tng.run();

        assertThat(listener.getSucceedMethodNames()).containsExactly("succeedTest", "succeedTest2");
        assertThat(listener.getFailedMethodNames()).containsExactly("failedTest"); // , "failedTest2");
        assertThat(listener.getSkippedMethodNames()).containsExactly("skippedTest");
    }
}
