package test.invocationcount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.invocationcount.issue426.SampleTestClassWithNoThreadPoolSizeDefined;
import test.invocationcount.issue426.SampleTestClassWithThreadPoolAndFailingFirstTimeConfig;
import test.invocationcount.issue426.SampleTestClassWithThreadPoolAndFirstLastTimeConfigs;
import test.invocationcount.issue426.SampleTestClassWithThreadPoolSizeDefined;

/**
 * Test various combination of @BeforeMethod(firstTimeOnly = true/false) and
 * AfterMethod(lastTimeOnly = true/false) with invocation counts and data providers.
 */
public class FirstAndLastTimeTest extends SimpleBaseTest {

  @Test
  public void verifyDataProviderFalseFalse() {
    List<String> invokedMethodNames = run(DataProviderFalseFalseTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod",
            "f(0)",
            "afterMethod",
            "beforeMethod",
            "f(1)",
            "afterMethod",
            "beforeMethod",
            "f(2)",
            "afterMethod",
            "beforeMethod",
            "f2(0)",
            "afterMethod",
            "beforeMethod",
            "f2(1)",
            "afterMethod",
            "beforeMethod",
            "f2(2)",
            "afterMethod");
  }

  @Test
  public void verifyDataProviderTrueFalse() {
    List<String> invokedMethodNames = run(DataProviderTrueFalseTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod",
            "f(0)",
            "afterMethod",
            "f(1)",
            "afterMethod",
            "f(2)",
            "afterMethod",
            "beforeMethod",
            "f2(0)",
            "afterMethod",
            "f2(1)",
            "afterMethod",
            "f2(2)",
            "afterMethod");
  }

  @Test
  public void verifyDataProviderFalseTrue() {
    List<String> invokedMethodNames = run(DataProviderFalseTrueTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod",
            "f(0)",
            "beforeMethod",
            "f(1)",
            "beforeMethod",
            "f(2)",
            "afterMethod",
            "beforeMethod",
            "f2(0)",
            "beforeMethod",
            "f2(1)",
            "beforeMethod",
            "f2(2)",
            "afterMethod");
  }

  @Test
  public void verifyDataProviderTrueTrue() {
    List<String> invokedMethodNames = run(DataProviderTrueTrueTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod",
            "f(0)",
            "f(1)",
            "f(2)",
            "afterMethod",
            "beforeMethod",
            "f2(0)",
            "f2(1)",
            "f2(2)",
            "afterMethod");
  }

  @Test
  public void verifyInvocationCountFalseFalse() {
    List<String> invokedMethodNames = run(InvocationCountFalseFalseTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod",
            "f",
            "afterMethod",
            "beforeMethod",
            "f",
            "afterMethod",
            "beforeMethod",
            "f",
            "afterMethod");
  }

  @Test
  public void verifyInvocationCountTrueFalse() {
    List<String> invokedMethodNames = run(InvocationCountTrueFalseTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod", "f", "afterMethod", "f", "afterMethod", "f", "afterMethod");
  }

  @Test
  public void verifyInvocationCountFalseTrue() {
    List<String> invokedMethodNames = run(InvocationCountFalseTrueTest.class);

    assertThat(invokedMethodNames)
        .containsExactly(
            "beforeMethod", "f", "beforeMethod", "f", "beforeMethod", "f", "afterMethod");
  }

  @Test
  public void verifyInvocationCountTrueTrue() {
    List<String> invokedMethodNames = run(InvocationCountTrueTrueTest.class);

    assertThat(invokedMethodNames).containsExactly("beforeMethod", "f", "f", "f", "afterMethod");
  }

  @Test(dataProvider = "classNames", description = "GITHUB-426")
  public void verifyFirstTimeOnly(Class<?> clazz) {
    List<String> invokedMethodNames = run(clazz);
    String[] expected = new String[] {"beforeMethod", "testMethod", "testMethod"};
    // The order is asserted on purpose, including for the sample class that runs its
    // invocations through a thread pool: a firstTimeOnly @BeforeMethod must complete
    // *before the first test invocation*, even when the invocations run in parallel.
    // The engine enforces this by running the firstTimeOnly configuration once, as a
    // barrier, before the pool starts (see TestInvoker#invokePooledTestMethods).
    assertThat(invokedMethodNames).containsExactly(expected);
  }

  @DataProvider(name = "classNames")
  public Object[][] getClassNames() {
    return new Object[][] {
      {SampleTestClassWithNoThreadPoolSizeDefined.class},
      {SampleTestClassWithThreadPoolSizeDefined.class}
    };
  }

  @Test(description = "GITHUB-426")
  public void verifyFirstAndLastTimeOnlyWithThreadPool() {
    List<String> invokedMethodNames =
        run(SampleTestClassWithThreadPoolAndFirstLastTimeConfigs.class);
    // Even though the two invocations run in parallel through a thread pool, the
    // firstTimeOnly @BeforeMethod must run once before any invocation and the
    // lastTimeOnly @AfterMethod once after every invocation has completed.
    assertThat(invokedMethodNames)
        .containsExactly("beforeMethod", "testMethod", "testMethod", "afterMethod");
  }

  @Test(description = "GITHUB-426")
  public void verifyFailingFirstTimeOnlySkipsAllParallelInvocations() {
    InvokedMethodNameListener listener =
        runWithListener(SampleTestClassWithThreadPoolAndFailingFirstTimeConfig.class);
    // The firstTimeOnly @BeforeMethod is shared by every parallel invocation and is run
    // once, as a barrier, before the pool starts. When it fails, every invocation must be
    // skipped and none must be executed - the failure must not be swallowed by the pool.
    assertThat(listener.getFailedMethodNames()).containsExactly("beforeMethod");
    assertThat(listener.getSkippedMethodNames()).containsExactly("testMethod", "testMethod");
    assertThat(listener.getSucceedMethodNames()).isEmpty();
  }

  private static List<String> run(Class<?> cls) {
    return runWithListener(cls).getInvokedMethodNames();
  }

  private static InvokedMethodNameListener runWithListener(Class<?> cls) {
    TestNG tng = create(cls);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    return listener;
  }
}
