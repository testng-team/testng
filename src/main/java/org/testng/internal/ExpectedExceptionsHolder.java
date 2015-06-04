package org.testng.internal;

import org.testng.ITestNGMethod;
import org.testng.TestException;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A class that contains the expected exceptions and the message regular expression.
 * @author cbeust
 */
public class ExpectedExceptionsHolder extends AbstractExpectedExceptionsHolder {
  private final String messageRegExp;

  public ExpectedExceptionsHolder(Class<?>[] expectedClasses, String messageRegExp) {
    super(expectedClasses);
    this.messageRegExp = messageRegExp;
  }

  /**
   *   message / regEx  .*      other
   *   null             true    false
   *   non-null         true    match
   */
  @Override
  protected boolean isExceptionMatches(Throwable ite) {
    if (".*".equals(messageRegExp)) {
      return true;
    } else {
      final String message = ite.getMessage();
      return message != null && Pattern.compile(messageRegExp, Pattern.DOTALL).matcher(ite.getMessage()).matches();
    }
  }

  protected String getWrongExceptionMessage(Throwable ite) {
    return "The exception was thrown with the wrong message:" +
           " expected \"" + messageRegExp + "\"" +
           " but got \"" + ite.getMessage() + "\"";
  }
}
