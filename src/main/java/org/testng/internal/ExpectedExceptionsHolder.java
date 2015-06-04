package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.TestException;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A class that contains the expected exceptions and the message regular expression.
 * @author cbeust
 */
public class ExpectedExceptionsHolder {
  private final Class<?>[] expectedClasses;
  private final String messageRegExp;

  public ExpectedExceptionsHolder(Class<?>[] expectedClasses, String messageRegExp) {
    this.expectedClasses = expectedClasses;
    this.messageRegExp = messageRegExp;
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
      if (exception.isAssignableFrom(realExceptionClass)) {
        return true;
      }
    }

    return false;
  }

  /**
   *   message / regEx  .*      other
   *   null             true    false
   *   non-null         true    match
   */
  public boolean messageRegExpMatches(Throwable ite) {
    if (".*".equals(messageRegExp)) {
      return true;
    } else {
      final String message = ite.getMessage();
      return message != null && Pattern.compile(messageRegExp, Pattern.DOTALL).matcher(ite.getMessage()).matches();
    }
  }

  public TestException buildTestException(Throwable ite) {
    return new TestException("The exception was thrown with the wrong message:" +
                      " expected \"" + messageRegExp + "\"" +
                      " but got \"" + ite.getMessage() + "\"", ite);
  }

  public TestException buildTestExceptionPluralize(Throwable ite) {
    return new TestException("Expected exception of " +
                             getExpectedExceptionsPluralize()
                             + " but got " + ite, ite);
  }

  public TestException buildTestExceptionPluralize(ITestNGMethod testMethod) {
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
}
