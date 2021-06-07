package test.hook.issue2251;

import java.lang.reflect.InvocationTargetException;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

/**
 * This test class apes on a bare essential What Spring TestNG provides as a base class in terms of
 * running Spring based tests. The following methods have been duplicated from
 * org.springframework.test.context.testng.AbstractTestNGSpringContextTests to simulate the bug. 1.
 * throwAsUncheckedException() 2. getTestResultException() 3. throwAs()
 */
public class AbstractBaseTestCase implements IHookable {

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    callBack.runTestMethod(testResult);
    Throwable t = getTestResultException(testResult);
    if (t != null) {
      throwAsUncheckedException(t);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends Throwable> void throwAs(Throwable t) throws T {
    throw (T) t;
  }

  private void throwAsUncheckedException(Throwable t) {
    throwAs(t);
  }

  private Throwable getTestResultException(ITestResult testResult) {
    Throwable testResultException = testResult.getThrowable();
    if (testResultException instanceof InvocationTargetException) {
      testResultException = testResultException.getCause();
    }
    return testResultException;
  }
}
