package org.testng;

public interface IExpectedExceptionsHolder {

  /**
   * Get the message in case the Throwable thrown by the test is not matching.
   *
   * @param ite The Throwable thrown by the test
   * @return The message which will be displayed as test result
   */
  String getWrongExceptionMessage(Throwable ite);

  /**
   * Check if the Throwable thrown by the test is matching with the holder logic
   *
   * @param ite The Throwable thrown by the test
   * @return true if the Throwable is matching with the holder logic, false otherwise
   */
  boolean isThrowableMatching(Throwable ite);
}
