package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.TestException;

import java.util.Arrays;

public abstract class AbstractExpectedExceptionsHolder {

  private final Class<?>[] expectedClasses;

  protected AbstractExpectedExceptionsHolder(Class<?>[] expectedClasses) {
    this.expectedClasses = expectedClasses;
  }

  /**
   * @param ite The exception that was just thrown
   * @return true if the exception that was just thrown is part of the
   * expected exceptions
   */
  public boolean isExpectedException(Throwable ite) {
    if (expectedClasses == null) {
      return false;
    }

    // TestException is the wrapper exception that TestNG will be throwing when an exception was
    // expected but not thrown
    if (ite.getClass() == TestException.class) {
      return false;
    }

    Class<?> realExceptionClass= ite.getClass();

    for (Class<?> exception : expectedClasses) {
      if (exception.isAssignableFrom(realExceptionClass) && isExceptionMatches(ite)) {
        return true;
      }
    }

    return false;
  }

  public TestException wrongException(Throwable ite) {
    if (isExceptionMatches(ite)) {
      return new TestException("Expected exception of " +
                               getExpectedExceptionsPluralize()
                               + " but got " + ite, ite);
    } else {
      return new TestException(getWrongExceptionMessage(ite), ite);
    }
  }

  protected abstract String getWrongExceptionMessage(Throwable ite);

  public TestException noException(ITestNGMethod testMethod) {
    if (expectedClasses == null || expectedClasses.length == 0) {
      return null;
    }
    return new TestException("Method " + testMethod + " should have thrown an exception of "
                             + getExpectedExceptionsPluralize());
  }

  private String getExpectedExceptionsPluralize() {
    StringBuilder sb = new StringBuilder();
    if (expectedClasses.length > 1) {
      sb.append("any of types ");
      sb.append(Arrays.toString(expectedClasses));
    } else {
      sb.append("type ");
      sb.append(expectedClasses[0]);
    }
    return sb.toString();
  }

  protected abstract boolean isExceptionMatches(Throwable ite);
}
