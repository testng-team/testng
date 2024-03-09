package org.testng;

/**
 * This is a marker interface for all objects that can be passed as a -listener argument.
 *
 * @author cbeust
 */
public interface ITestNGListener {

  /** @return - <code>true</code> if the current listener can be considered for execution. */
  default boolean isEnabled() {
    return true;
  }
}
