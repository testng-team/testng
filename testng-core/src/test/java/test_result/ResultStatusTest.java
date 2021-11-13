package test_result;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test_result.AfterListenerSample.MySkipTestListener;

public class ResultStatusTest extends SimpleBaseTest {

  @Test
  public void testGitHub1197() {
    TestNG tng = create(GitHub1197Sample.class);

    // A skipped method is not supposed to be run but, here, it's the goal of the feature
    InvokedMethodNameListener listener = new InvokedMethodNameListener(true, true);
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("succeedTest", "succeedTest2");
    assertThat(listener.getFailedBeforeInvocationMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).containsExactly("failedTest"); // , "failedTest2");
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSkippedAfterInvocationMethodNames()).containsExactly("skippedTest");
  }

  @Test
  public void testBeforeListener() {
    TestNG tng = create(BeforeListenerSample.class);

    // A skipped method is not supposed to be run but, here, it's the goal of the feature
    InvokedMethodNameListener listener = new InvokedMethodNameListener(true, true);
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("succeedTest", "succeedTest2");
    assertThat(listener.getFailedBeforeInvocationMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).containsExactly("failedTest"); // , "failedTest2");
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSkippedAfterInvocationMethodNames()).containsExactly("skippedTest");
  }

  @Test
  public void testAfterListener() {
    TestNG tng = create(AfterListenerSample.class);
    AfterListenerSample.MySkipTestListener testCaseListener = new MySkipTestListener();

    // A skipped method is not supposed to be run but, here, it's the goal of the feature
    InvokedMethodNameListener listener = new InvokedMethodNameListener(true, true);
    OrderAbidingListener orderAbidingListener =
        new OrderAbidingListener(testCaseListener, listener);
    tng.addListener(orderAbidingListener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("succeedTest", "succeedTest2");
    assertThat(listener.getFailedBeforeInvocationMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).containsExactly("failedTest"); // , "failedTest2");
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSkippedAfterInvocationMethodNames()).containsExactly("skippedTest");
  }
}
