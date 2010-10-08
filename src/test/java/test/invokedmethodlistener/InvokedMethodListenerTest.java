package test.invokedmethodlistener;

import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

import java.util.List;

public class InvokedMethodListenerTest extends SimpleBaseTest {

  private void run(Class[] classes, MyListener l) {
    TestNG tng = create();
    tng.setTestClasses(classes);

    tng.addInvokedMethodListener(l);
    tng.run();

    Assert.assertEquals(l.getBeforeCount(), 9);
    Assert.assertEquals(l.getAfterCount(), 9);
  }

  @Test
  public void withSuccess() {
    MyListener l = new MyListener();
    run(new Class[] { Success.class }, l);
  }

  @Test
  public void withFailure() {
    MyListener l = new MyListener();
    run(new Class[] { Failure.class }, l);
    Assert.assertEquals(l.getSuiteStatus(), ITestResult.FAILURE);
    Assert.assertTrue(null != l.getSuiteThrowable());
    Assert.assertTrue(l.getSuiteThrowable().getClass() == RuntimeException.class);

    Assert.assertEquals(l.getMethodStatus(), ITestResult.FAILURE);
    Assert.assertTrue(null != l.getMethodThrowable());
    Assert.assertTrue(l.getMethodThrowable().getClass() == IllegalArgumentException.class);
  }

  /**
   * Fix for:
   * http://code.google.com/p/testng/issues/detail?id=7
   * http://code.google.com/p/testng/issues/detail?id=86
   */
  @Test
  public void sameMethodInvokedMultipleTimesShouldHaveDifferentTimeStamps() {
    TestNG tng = create(Sample.class);
    tng.addListener(new InvokedMethodListener());
    tng.run();
    List<IInvokedMethod> m = InvokedMethodListener.m_methods;
//    for (IInvokedMethod mm : m) {
//      System.out.println(mm.getTestMethod().getMethodName() + " " + mm.getDate());
//    }
    IInvokedMethod after1 = m.get(1);
    Assert.assertTrue(after1.getTestMethod().isAfterMethodConfiguration());
    IInvokedMethod after2 = m.get(3);
    Assert.assertTrue(after2.getTestMethod().isAfterMethodConfiguration());
    Assert.assertTrue(after1.getDate() != after2.getDate());
  }

  @Test(description = "Test methods with expected exceptions should show up as pass" +
  		" in IInvokedMethodListener's afterInvocaiton method")
  public void testMethodsWithExpectedExceptionsShouldShowUpAsPass() {
    TestNG tng = create(Sample2.class);
    Sample2.Sample2InvokedMethodListener l = new Sample2().new Sample2InvokedMethodListener();
    tng.addListener(l);
    tng.run();

    Assert.assertTrue(l.isSuccess);
  }
}
