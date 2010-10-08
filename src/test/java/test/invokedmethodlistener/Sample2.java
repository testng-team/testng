package test.invokedmethodlistener;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class Sample2 {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void t1() {
    throw new IllegalArgumentException("Throw this exception on purpose in test");
  }

  public class Sample2InvokedMethodListener implements IInvokedMethodListener
  {

     boolean isSuccess = false;

     /**
      * {@inheritDoc}
      */
     @Override
    public void afterInvocation(IInvokedMethod m, ITestResult tr)
     {
        isSuccess = tr.isSuccess();
     }

     /**
      * {@inheritDoc}
      */
     @Override
    public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1)
     {
        // no need to implement this right now
     }

     public boolean isSuccess() {
        return isSuccess;
     }
  }
}
