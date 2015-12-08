package org.testng;

import java.io.File;
import java.io.IOException;

/**
 * Assertion tool for File centric assertions.
 * Conceptually this is an extension of {@link Assert}
 * Presents assertion methods with a more natural parameter order.
 * The order is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:pmendelson@trueoutcomes.com'>Paul Mendelon</a>
 * @since 5.6
 * @version $Revision: 650 $, $Date: 2009-01-05 03:51:54 -0800 (Mon, 05 Jan 2009) $
 */
public class FileAssert {

  /**
   * Protect constructor since it is a static only class
   */
  private FileAssert() {
  	// hide constructor
  }

  /**
   * Asserts that a {@code tstvalue} is a proper directory. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertDirectory(File tstvalue, String message) {
  	boolean condition=false;
  	try {
  	condition=tstvalue.isDirectory();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,fileType(tstvalue),"Directory", message);
  	}
  	if(!condition) {
  	failFile(tstvalue,fileType(tstvalue),"Directory", message);
  	}
  }

  static public void assertDirectory(File tstvalue) {
  	assertDirectory(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a proper file. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertFile(File tstvalue, String message) {
  	boolean condition=false;
  	try {
  	condition=tstvalue.isFile();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,fileType(tstvalue),"File", message);
  	}
  	if(!condition) {
  	failFile(tstvalue,fileType(tstvalue),"File", message);
  	}
  }

  /**
   * @see #assertFile(File, String)
   */
  static public void assertFile(File tstvalue) {
  	assertFile(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a file of exactly {@code expected} characters
   * or a directory of exactly {@code expected} entries. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertLength(File tstvalue, long expected, String message) {
  	long actual=-1L;
  	try {
  	actual=tstvalue.isDirectory()?tstvalue.list().length:tstvalue.length();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,String.valueOf(actual),String.valueOf(expected), message);
  	}
  	if(actual!=expected) {
  	failFile(tstvalue,String.valueOf(actual),String.valueOf(expected), message);
  	}
  }

  /**
   * @see #assertLength(File, long, String)
   */
  static public void assertLength(File tstvalue, long expected) {
  	assertLength(tstvalue, expected, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a file of at least {@code expected} characters
   * or a directory of at least {@code expected} entries. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertMinLength(File tstvalue, long expected, String message) {
  	long actual=-1L;
  	try {
  	actual=tstvalue.isDirectory()?tstvalue.list().length:tstvalue.length();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,String.valueOf(actual),"at least "+String.valueOf(expected), message);
  	}
  	if(actual<expected) {
  	failFile(tstvalue,String.valueOf(actual),"at least "+String.valueOf(expected), message);
  	}
  }

  /**
   * @see #assertMinLength(File, long, String)
   */
  static public void assertMinLength(File tstvalue, long expected) {
  	assertMinLength(tstvalue, expected, null);
  }

  /**
   * Asserts that a {@code tstvalue} is a file of at most {@code expected} characters
   * or a directory of at most {@code expected} entries. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertMaxLength(File tstvalue, long expected, String message) {
  	long actual=-1L;
  	try {
  	actual=tstvalue.isDirectory()?tstvalue.list().length:tstvalue.length();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,String.valueOf(actual),"at most "+String.valueOf(expected), message);
  	}
  	if(actual>expected) {
  	failFile(tstvalue,String.valueOf(actual),"at most "+String.valueOf(expected), message);
  	}
  }

  /**
   * @see #assertMaxLength(File, long, String)
   */
  static public void assertMaxLength(File tstvalue, long expected) {
  	assertMaxLength(tstvalue, expected, null);
  }

  /**
   * Asserts that a {@code tstvalue} is readable. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertReadable(File tstvalue, String message) {
  	boolean condition=false;
  	try {
  	condition=tstvalue.canRead();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,fileAccess(tstvalue),"Read Access", message);
  	}
  	if(!condition) {
  	failFile(tstvalue,fileAccess(tstvalue),"Read Access", message);
  	}
  }

  /**
   * @see #assertReadable(File, String)
   */
  static public void assertReadable(File tstvalue) {
  	assertReadable(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is writeable. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertWriteable(File tstvalue, String message) {
  	boolean condition=false;
  	try {
  	condition=tstvalue.canWrite();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,fileAccess(tstvalue),"Write Access", message);
  	}
  	if(!condition) {
  	failFile(tstvalue,fileAccess(tstvalue),"Write Access", message);
  	}
  }

  /**
   * @see #assertWriteable(File, String)
   */
  static public void assertWriteable(File tstvalue) {
  	assertReadable(tstvalue, null);
  }

  /**
   * Asserts that a {@code tstvalue} is readable and writeable. If it isn't,
   * an AssertionError, with the given message, is thrown.
   * @param tstvalue the file to evaluate
   * @param message the assertion error message
   */
  static public void assertReadWrite(File tstvalue, String message) {
  	boolean condition=false;
  	try {
  	condition=tstvalue.canRead() && tstvalue.canWrite();
  	} catch(SecurityException e) {
  	failSecurity(e,tstvalue,fileAccess(tstvalue),"Read/Write Access", message);
  	}
  	if(!condition) {
  	failFile(tstvalue,fileAccess(tstvalue),"Read/Write Access", message);
  	}
  }

  /**
   * @see #assertReadWrite(File, String)
   */
  static public void assertReadWrite(File tstvalue) {
  	assertReadWrite(tstvalue, null);
  }

  /**
   * Fails a test with the given message and wrapping the original exception.
   *
   * @param message the assertion error message
   * @param realCause the original exception
   */
  static public void fail(String message, Throwable realCause) {
  	AssertionError ae = new AssertionError(message);
  	ae.initCause(realCause);

  	throw ae;
  }

  /**
   * Fails a test with the given message.
   * @param message the assertion error message
   */
  static public void fail(String message) {
  	throw new AssertionError(message);
  }

  /**
   * Fails a test with no message.
   */
  static public void fail() {
  	fail(null);
  }

  /**
   * Formats failure for file assertions
   */
  private static void failFile(File path, String actual, String expected, String message) {
  	String formatted = "";
  	if(message != null) {
  	formatted = message + " ";
  	}
  	fail(formatted + "expected <" + expected +"> but was <" + toString(path) + ">"
  		+(expected!=null?"<" + expected +">":""));
  }

  /**
   * @param tstvalue
   * @param string
   * @param string2
   * @param message
   */
  private static void failSecurity(Exception e, File path, String actual, String expected, String message) {
  	String formatted = "";
  	if(message != null) {
  	formatted = message + " ";
  	}
  	fail(formatted + "expected <" + expected +"> but was <" + toString(path) + ">"
  		+"<"
  		+ (e!=null && e.getMessage()!=null && e.getMessage().length()>0
  			?e.getMessage()
  			:"not authorized by JVM")
  		+ ">");
  }

  /**
   * String representation of what sort of file {@code path} is.
   */
  private static String fileType(File path) {
  	try {
  	if(!path.exists()) {
      return "Non existant";
    } else if (path.isDirectory()) {
      return "Directory";
    } else if (path.isFile()) {
      return "File";
    } else {
      return "Special File";
    }
  	} catch (SecurityException e) {
  	return "Unauthorized";
  	}
  }

  /**
   * String representation of what sort of file {@code path} is.
   */
  private static String fileAccess(File path) {
  	try {
  	if(!path.exists()) {
      return "Non existant";
    } else if (path.canWrite() && path.canRead()) {
      return "Read/Write Access";
    } else if (path.canRead()) {
      return "Read only Access";
    } else if (path.canWrite()) {
      return "Write only Access";
    } else {
      return "No Access";
    }
  	} catch (SecurityException e) {
  	return "Unauthorized";
  	}
  }

  private static String toString(File path) {
  	try {
  	return path.getCanonicalPath();
  	} catch(IOException e) {
  	return path.getAbsolutePath();
  	}
  }
}
