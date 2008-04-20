package test.invokedmethodlistener;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class InvokedMethodListenerTest extends SimpleBaseTest {
  
  private void run(Class[] classes) {
    TestNG tng = create();
    tng.setTestClasses(classes);
    MyListener l = new MyListener();
    tng.addInvokedMethodListener(l);
    tng.run();
    
    Assert.assertEquals(l.getBeforeCount(), 9);
    Assert.assertEquals(l.getAfterCount(), 9);
  }
  
  @Test
  public void withSuccess() {
    run(new Class[] { Success.class });
  }
  
  @Test
  public void withFailure() {
    run(new Class[] { Failure.class });
  }
}
