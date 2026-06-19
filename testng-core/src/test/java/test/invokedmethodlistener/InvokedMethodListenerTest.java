package test.invokedmethodlistener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class InvokedMethodListenerTest extends SimpleBaseTest {

  private static void run(Class[] classes, IInvokedMethodListener l) {
    TestNG tng = create();
    tng.setTestClasses(classes);

    tng.addListener((ITestNGListener) l);
    tng.run();
  }

  private static void assertMethodCount(MyListener l) {
    assertThat(l.getBeforeCount()).isEqualTo(9);
    assertThat(l.getAfterCount()).isEqualTo(9);
  }

  @Test
  public void withSuccess() {
    MyListener l = new MyListener();
    run(new Class[] {Success.class}, l);
    assertMethodCount(l);
  }

  @Test
  public void withFailure() {
    MyListener l = new MyListener();
    run(new Class[] {Failure.class}, l);
    assertMethodCount(l);
    assertThat(l.getSuiteStatus()).isEqualTo(ITestResult.FAILURE);
    assertThat(l.getSuiteThrowable()).isNotNull();
    assertThat(l.getSuiteThrowable().getClass()).isSameAs(RuntimeException.class);

    assertThat(l.getMethodStatus()).isEqualTo(ITestResult.FAILURE);
    assertThat(l.getMethodThrowable()).isNotNull();
    assertThat(l.getMethodThrowable().getClass()).isSameAs(IllegalArgumentException.class);
  }

  /**
   * Fix for: https://github.com/juherr/testng-googlecode/issues/7
   * https://github.com/juherr/testng-googlecode/issues/86
   * https://github.com/cbeust/testng/issues/93
   */
  @Test
  public void sameMethodInvokedMultipleTimesShouldHaveDifferentTimeStamps() {
    TestNG tng = create(Sample.class);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener((ITestNGListener) listener);
    tng.run();
    List<IInvokedMethod> m = listener.getInvokedMethods();
    IInvokedMethod beforeSuite = m.get(0);
    assertThat(beforeSuite.getTestMethod().isAfterMethodConfiguration()).isFalse();
    assertThat(beforeSuite.isConfigurationMethod()).isTrue();
    IInvokedMethod after1 = m.get(2);
    assertThat(after1.getTestMethod().isAfterMethodConfiguration()).isTrue();
    assertThat(after1.isConfigurationMethod()).isTrue();
    IInvokedMethod after2 = m.get(4);
    assertThat(after2.getTestMethod().isAfterMethodConfiguration()).isTrue();
    assertThat(after2.isConfigurationMethod()).isTrue();
    assertThat(after1.getDate()).isNotSameAs(after2.getDate());
  }

  @Test(
      description =
          "Test methods with expected exceptions should show up as pass"
              + " in IInvokedMethodListener's afterInvocation method")
  public void testMethodsWithExpectedExceptionsShouldShowUpAsPass() {
    TestNG tng = create(Sample2.class);
    Sample2.Sample2InvokedMethodListener l = new Sample2().new Sample2InvokedMethodListener();
    tng.addListener((ITestNGListener) l);
    tng.run();

    assertThat(l.isSuccess).isTrue();
  }

  @Test(description = "Invoked method does not recognize configuration method")
  public void issue629_InvokedMethodDoesNotRecognizeConfigurationMethod() {
    InvokedMethodNameListener l = new InvokedMethodNameListener();
    run(new Class[] {Success.class}, l);

    assertThat(l.testMethods).hasSize(1).contains("a");

    assertThat(l.testMethodsFromTM).hasSize(1).contains("a");

    assertThat(l.configurationMethods)
        .hasSize(8)
        .contains("beforeMethod")
        .contains("afterMethod")
        .contains("beforeTest")
        .contains("afterTest")
        .contains("beforeClass")
        .contains("afterClass")
        .contains("beforeSuite")
        .contains("afterSuite");

    assertThat(l.configurationMethodsFromTM)
        .hasSize(8)
        .contains("beforeMethod")
        .contains("afterMethod")
        .contains("beforeTest")
        .contains("afterTest")
        .contains("beforeClass")
        .contains("afterClass")
        .contains("beforeSuite")
        .contains("afterSuite");
  }

  @Test
  public void issue87_method_orderning_with_disable_test_class() {
    assertIssue87(A.class, B.class, C.class);
    assertIssue87(A.class, C.class, B.class);
    assertIssue87(B.class, A.class, C.class);
  }

  private void assertIssue87(Class<?>... tests) {
    TestNG tng = create(tests);
    tng.setParallel(XmlSuite.ParallelMode.NONE);
    tng.setPreserveOrder(true);
    InvokedMethodListener listener = new InvokedMethodListener();
    tng.addListener((ITestNGListener) listener);
    tng.run();
    List<IInvokedMethod> m = listener.getInvokedMethods();
    assertThat(m.get(0).getTestMethod().getMethodName()).isEqualTo("someMethod1");
    assertThat(m.get(1).getTestMethod().getMethodName()).isEqualTo("someMethod3");
    assertThat(m.get(2).getTestMethod().getMethodName()).isEqualTo("someTest");
    assertThat(m).hasSize(3);
  }
}
