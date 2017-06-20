package test_result;

import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHub1197Test extends SimpleBaseTest {

    @Test
    public void testGitHub1197() {
        TestNG tng = create(GitHub1197Sample.class);

        // A skipped method is not supposed to be run but, here, it's the goal of the feature
        InvokedMethodNameListener listener = new InvokedMethodNameListener(true, true);
        tng.addListener((ITestNGListener) listener);

        tng.run();

        doAssert(listener.getSucceedMethodNames(),"succeedTest", "succeedTest2");
        doAssert(listener.getFailedMethodNames(),"failedTest"); // , "failedTest2");
        doAssert(listener.getSkippedAfterInvocationMethodNames(),"skippedTest");
    }

}
