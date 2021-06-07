package org.testng;

import java.io.File;
import java.io.IOException;

/**
 * Assertion tool for File centric assertions. Conceptually, this is an extension of {@link Assert}.
 * Presents assertion methods with a more natural parameter order. The order is always
 * <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:pmendelson@trueoutcomes.com'>Paul Mendelon</a>
 * @since 5.6
 */
public class FileAssert {

  /** Protect this constructor since it is a static only class. */
  private FileAssert() {
    // Hide this constructor.
  }

  /**
   * Asserts that a {@code tstvalue} is a proper directory. If it isn't, an AssertionError with the
   * given message is thrown.
   *
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  public static void assertDirectory(File tstvalue, String message) {
    boolean condition = false;
    try {
      condition = tstvalue.isDirectory();
    } catch (SecurityException e) {
      failSecurity(e, tstvalue, fileType(tstvalue), "Directory", message);
    }
    if (!condition) {
      failFile(tstvalue, fileType(tstvalue), "Directory", message);
    }
  }

  public static void assertDirectory(File tstvalue) {
    assertDirectory(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a proper file. If it isn't, an AssertionError with the given
   * message is thrown.
   *
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  public static void assertFile(File tstvalue, String message) {
    boolean condition = false;
    try {
      condition = tstvalue.isFile();
    } catch (SecurityException e) {
      failSecurity(e, tstvalue, fileType(tstvalue), "File", message);
    }
    if (!condition) {
      failFile(tstvalue, fileType(tstvalue), "File", message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @see #assertFile(File, String)
   */
  public static void assertFile(File tstvalue) {
    assertFile(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a file of exactly {@code expected} characters or a directory
   * of exactly {@code expected} entries. If it isn't, an AssertionError with the given message is
   * thrown.
   *
   * @param tstvalue the file to evaluate
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertLength(File tstvalue, long expected, String message) {
    long actual = -1L;
    try {
      actual = tstvalue.isDirectory() ? tstvalue.list().length : tstvalue.length();
    } catch (SecurityException e) {
      failSecurity(e, tstvalue, String.valueOf(actual), String.valueOf(expected), message);
    }
    if (actual != expected) {
      failFile(tstvalue, String.valueOf(actual), String.valueOf(expected), message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @param expected The expected length
   * @see #assertLength(File, long, String)
   */
  public static void assertLength(File tstvalue, long expected) {
    assertLength(tstvalue, expected, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a file of at least {@code expected} characters or a
   * directory of at least {@code expected} entries. If it isn't, an AssertionError with the given
   * message is thrown.
   *
   * @param tstvalue the file to evaluate
   * @param expected the expected value
   * @param message the assertion error message
   */
  public static void assertMinLength(File tstvalue, long expected, String message) {
    long actual = -1L;
    try {
      actual = tstvalue.isDirectory() ? tstvalue.list().length : tstvalue.length();
    } catch (SecurityException e) {
      failSecurity(
          e, tstvalue, String.valueOf(actual), "at least " + String.valueOf(expected), message);
    }
    if (actual < expected) {
      failFile(tstvalue, String.valueOf(actual), "at least " + String.valueOf(expected), message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @param expected The expected min length
   * @see #assertMinLength(File, long, String)
   */
  public static void assertMinLength(File tstvalue, long expected) {
    assertMinLength(tstvalue, expected, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a file of at most {@code expected} characters or a directory
   * of at most {@code expected} entries. If it isn't, an AssertionError with the given message is
   * thrown.
   *
   * @param tstvalue the file to evaluate
   * @param expected The expected max length
   * @param message the assertion error message
   */
  public static void assertMaxLength(File tstvalue, long expected, String message) {
    long actual = -1L;
    try {
      actual = tstvalue.isDirectory() ? tstvalue.list().length : tstvalue.length();
    } catch (SecurityException e) {
      failSecurity(
          e, tstvalue, String.valueOf(actual), "at most " + String.valueOf(expected), message);
    }
    if (actual > expected) {
      failFile(tstvalue, String.valueOf(actual), "at most " + String.valueOf(expected), message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @param expected The expected length
   * @see #assertMaxLength(File, long, String)
   */
  public static void assertMaxLength(File tstvalue, long expected) {
    assertMaxLength(tstvalue, expected, null);
  }

  /**
   * Asserts that a {@code tstvalue} is readable. If it isn't, an AssertionError with the given
   * message is thrown.
   *
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  public static void assertReadable(File tstvalue, String message) {
    boolean condition = false;
    try {
      condition = tstvalue.canRead();
    } catch (SecurityException e) {
      failSecurity(e, tstvalue, fileAccess(tstvalue), "Read Access", message);
    }
    if (!condition) {
      failFile(tstvalue, fileAccess(tstvalue), "Read Access", message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @see #assertReadable(File, String)
   */
  public static void assertReadable(File tstvalue) {
    assertReadable(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is writeable. If it isn't, an AssertionError with the given
   * message is thrown.
   *
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  public static void assertWriteable(File tstvalue, String message) {
    boolean condition = false;
    try {
      condition = tstvalue.canWrite();
    } catch (SecurityException e) {
      failSecurity(e, tstvalue, fileAccess(tstvalue), "Write Access", message);
    }
    if (!condition) {
      failFile(tstvalue, fileAccess(tstvalue), "Write Access", message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @see #assertWriteable(File, String)
   */
  public static void assertWriteable(File tstvalue) {
    assertReadable(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is readable and writeable. If it isn't, an AssertionError with
   * the given message is thrown.
   *
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  public static void assertReadWrite(File tstvalue, String message) {
    boolean condition = false;
    try {
      condition = tstvalue.canRead() && tstvalue.canWrite();
    } catch (SecurityException e) {
      failSecurity(e, tstvalue, fileAccess(tstvalue), "Read/Write Access", message);
    }
    if (!condition) {
      failFile(tstvalue, fileAccess(tstvalue), "Read/Write Access", message);
    }
  }

  /**
   * @param tstvalue The actual file
   * @see #assertReadWrite(File, String)
   */
  public static void assertReadWrite(File tstvalue) {
    assertReadWrite(tstvalue, null);
  }

  /**
   * Fails a test with the given message and wrapping the original exception.
   *
   * @param message the assertion error message
   * @param realCause the original exception
   */
  public static void fail(String message, Throwable realCause) {
    AssertionError ae = new AssertionError(message);
    ae.initCause(realCause);

    throw ae;
  }

  /**
   * Fails a test with the given message.
   *
   * @param message the assertion error message
   */
  public static void fail(String message) {
    throw new AssertionError(message);
  }

  /** Fails a test with no message. */
  public static void fail() {
    fail(null);
  }

  /** Formats failure for file assertions. */
  private static void failFile(File path, String actual, String expected, String message) {
    String formatted = "";
    if (message != null) {
      formatted = message + " ";
    }
    fail(
        formatted
            + "expected <"
            + expected
            + "> but was <"
            + toString(path)
            + ">"
            + (expected != null ? "<" + expected + ">" : ""));
  }

  /**
   * @param tstvalue
   * @param string
   * @param string2
   * @param message
   */
  private static void failSecurity(
      Exception e, File path, String actual, String expected, String message) {
    String formatted = "";
    if (message != null) {
      formatted = message + " ";
    }
    fail(
        formatted
            + "expected <"
            + expected
            + "> but was <"
            + toString(path)
            + ">"
            + "<"
            + (e != null && e.getMessage() != null && e.getMessage().length() > 0
                ? e.getMessage()
                : "not authorized by JVM")
            + ">");
  }

  /** String representation of what sort of file {@code path} is. */
  private static String fileType(File path) {
    try {
      if (!path.exists()) {
        return "Nonexistent";
      }
      if (path.isDirectory()) {
        return "Directory";
      }
      if (path.isFile()) {
        return "File";
      }
      return "Special File";
    } catch (SecurityException e) {
      return "Unauthorized";
    }
  }

  /** String representation of read and write permissions of {@code path}. */
  private static String fileAccess(File path) {
    try {
      if (!path.exists()) {
        return "Nonexistent";
      }
      if (path.canRead() && path.canWrite()) {
        return "Read and Write Access";
      }
      if (path.canRead()) {
        return "Read but not Write Access";
      }
      if (path.canWrite()) {
        return "Write but not Read Access";
      }
      return "Neither Read nor Write Access";
    } catch (SecurityException e) {
      return "Unauthorized";
    }
  }

  private static String toString(File path) {
    try {
      return path.getCanonicalPath();
    } catch (IOException e) {
      return path.getAbsolutePath();
    }
  }
}
