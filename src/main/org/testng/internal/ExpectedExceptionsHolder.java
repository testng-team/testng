package org.testng.internal;

/**
 * A class that contains the expected exceptions and the message regular expression.
 * @author cbeust
 */
public class ExpectedExceptionsHolder {
  Class<?>[] expectedClasses;
  String messageRegExp;

  public ExpectedExceptionsHolder(Class<?>[] expectedClasses, String messageRegExp) {
    this.expectedClasses = expectedClasses;
    this.messageRegExp = messageRegExp;
  }

}
