package org.testng.internal;

import org.testng.IExpectedExceptionsHolder;
import org.testng.ITestNGMethod;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.IAnnotationFinder;

import java.util.regex.Pattern;

/**
 * A class that contains the expected exceptions and the message regular expression.
 * @author cbeust
 */
public class RegexpExpectedExceptionsHolder implements IExpectedExceptionsHolder {
  public static final String DEFAULT_REGEXP = ".*";

  private final IAnnotationFinder finder;
  private final ITestNGMethod method;

  public RegexpExpectedExceptionsHolder(IAnnotationFinder finder, ITestNGMethod method) {
    this.finder = finder;
    this.method = method;
  }

  /**
   *   message / regEx  .*      other
   *   null             true    false
   *   non-null         true    match
   */
  @Override
  public boolean isThrowableMatching(Throwable ite) {
    String messageRegExp = getRegExp();

    if (DEFAULT_REGEXP.equals(messageRegExp)) {
      return true;
    }

    final String message = ite.getMessage();
    return message != null && Pattern.compile(messageRegExp, Pattern.DOTALL).matcher(message).matches();
  }

  public String getWrongExceptionMessage(Throwable ite) {
    return "The exception was thrown with the wrong message:" +
           " expected \"" + getRegExp() + "\"" +
           " but got \"" + ite.getMessage() + "\"";
  }

  private String getRegExp() {
    IExpectedExceptionsAnnotation expectedExceptions =
        finder.findAnnotation(method, IExpectedExceptionsAnnotation.class);
    if (expectedExceptions != null) {
      // Old syntax => default value
      return DEFAULT_REGEXP;
    }

    // New syntax
    ITestAnnotation testAnnotation = finder.findAnnotation(method, ITestAnnotation.class);
    if (testAnnotation != null) {
      return testAnnotation.getExpectedExceptionsMessageRegExp();
    }

    return DEFAULT_REGEXP;
  }
}
