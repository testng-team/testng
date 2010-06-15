package test.invokedmethodlistener;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

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
}
