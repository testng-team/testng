package org.testng.internal;

import org.testng.IExpectedExceptionsHolder;
import org.testng.ITestNGMethod;
import org.testng.TestException;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;

import java.util.Arrays;

public class ExpectedExceptionsHolder {

  protected final IAnnotationFinder finder;
  protected final ITestNGMethod method;
  private final Class<?>[] expectedClasses;
  private final IExpectedExceptionsHolder holder;

  protected ExpectedExceptionsHolder(IAnnotationFinder finder, ITestNGMethod method, IExpectedExceptionsHolder holder) {
    this.finder = finder;
    this.method = method;
    expectedClasses = findExpectedClasses(finder, method);
    this.holder = holder;
  }

  private static Class<?>[] findExpectedClasses(IAnnotationFinder finder, ITestNGMethod method) {
    IExpectedExceptionsAnnotation expectedExceptions =
        finder.findAnnotation(method, IExpectedExceptionsAnnotation.class);
    // Old syntax
    if (expectedExceptions != null) {
      return expectedExceptions.getValue();
    }

    // New syntax
    ITestAnnotation testAnnotation = finder.findAnnotation(method, ITestAnnotation.class);
    if (testAnnotation != null) {
      return testAnnotation.getExpectedExceptions();
    }

    return new Class<?>[0];
  }

  /**
   * @param ite The exception that was just thrown
   * @return true if the exception that was just thrown is part of the
   * expected exceptions
   */
  public boolean isExpectedException(Throwable ite) {
    if (!hasExpectedClasses()) {
      return false;
    }

    // TestException is the wrapper exception that TestNG will be throwing when an exception was
    // expected but not thrown
    if (ite.getClass() == TestException.class) {
      return false;
    }

    Class<?> realExceptionClass= ite.getClass();

    for (Class<?> exception : expectedClasses) {
      if (exception.isAssignableFrom(realExceptionClass) && holder.isThrowableMatching(ite)) {
        return true;
      }
    }

    return false;
  }

  public Throwable wrongException(Throwable ite) {
    if (!hasExpectedClasses()) {
      return ite;
    }

    if (holder.isThrowableMatching(ite)) {
      return new TestException("Expected exception of " +
                               getExpectedExceptionsPluralize()
                               + " but got " + ite, ite);
    } else {
      return new TestException(holder.getWrongExceptionMessage(ite), ite);
    }
  }

  public TestException noException(ITestNGMethod testMethod) {
    if (!hasExpectedClasses()) {
      return null;
    }
    return new TestException("Method " + testMethod + " should have thrown an exception of "
                             + getExpectedExceptionsPluralize());
  }

  private boolean hasExpectedClasses() {
    return expectedClasses != null && expectedClasses.length > 0;
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
}
