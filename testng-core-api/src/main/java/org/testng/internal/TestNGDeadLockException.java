package org.testng.internal;

import org.testng.TestNGException;

/** Represents a deadlock condition in TestNG wherein execution can get stalled. */
public class TestNGDeadLockException extends TestNGException {
  public TestNGDeadLockException(String string) {
    super(string);
  }
}
