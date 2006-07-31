package org.testng;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * Assertion tool class. Presents assertion methods with a more natural parameter order.
 * The order is always <B>actualValue</B>, <B>expectedValue</B> [, message].
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class Assert {

  /**
   * Protect constructor since it is a static only class
   */
  protected Assert() {
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionError with the given message.
   */
  static public void assertTrue(boolean condition, String message) {
    if(!condition) {
      failNotEquals(new Boolean(condition), new Boolean(true), message);
    }
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionError.
   */
  static public void assertTrue(boolean condition) {
    assertTrue(condition, null);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionError with the given message.
   */
  static public void assertFalse(boolean condition, String message) {
    assertTrue(!condition, message);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionError.
   */
  static public void assertFalse(boolean condition) {
    assertFalse(condition, null);
  }

  /**
   * Fails a test with the given message and wrapping the original exception.
   * 
   * @param message
   * @param realCause
   */
  static public void fail(String message, Throwable realCause) {
    AssertionError ae = new AssertionError(message);
    ae.initCause(realCause);
    
    throw ae;
  }
  
  /**
   * Fails a test with the given message.
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
   * Asserts that two objects are equal. If they are not
   * an AssertionError is thrown with the given message.
   */
  static public void assertEquals(Object actual, Object expected, String message) {
    if((expected == null) && (actual == null)) {
      return;
    }
    if((expected != null) && expected.equals(actual)) {
      return;
    }
    failNotEquals(actual, expected, message);
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionError is thrown.
   */
  static public void assertEquals(Object actual, Object expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two Strings are equal.
   */
  static public void assertEquals(String actual, String expected, String message) {
    assertEquals((Object) actual, (Object) expected, message);
  }

  /**
   * Asserts that two Strings are equal.
   */
  static public void assertEquals(String actual, String expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two doubles are equal concerning a delta.  If they are not
   * an AssertionError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(double actual, double expected, double delta, String message) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if(Double.isInfinite(expected)) {
      if(!(expected == actual)) {
        failNotEquals(new Double(actual), new Double(expected), message);
      }
    }
    else if(!(Math.abs(expected - actual) <= delta)) { // Because comparison with NaN always returns false
      failNotEquals(new Double(actual), new Double(expected), message);
    }
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(double actual, double expected, double delta) {
    assertEquals(actual, expected, delta, null);
  }

  /**
   * Asserts that two floats are equal concerning a delta. If they are not
   * an AssertionError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(float actual, float expected, float delta, String message) {
    // handle infinity specially since subtracting to infinite values gives NaN and the
    // the following test fails
    if(Float.isInfinite(expected)) {
      if(!(expected == actual)) {
        failNotEquals(new Float(actual), new Float(expected), message);
      }
    }
    else if(!(Math.abs(expected - actual) <= delta)) {
      failNotEquals(new Float(actual), new Float(expected), message);
    }
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  static public void assertEquals(float actual, float expected, float delta) {
    assertEquals(actual, expected, delta, null);
  }

  /**
   * Asserts that two longs are equal. If they are not
   * an AssertionError is thrown with the given message.
   */
  static public void assertEquals(long actual, long expected, String message) {
    assertEquals(new Long(actual), new Long(expected), message);
  }

  /**
   * Asserts that two longs are equal.
   */
  static public void assertEquals(long actual, long expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two booleans are equal. If they are not
   * an AssertionError is thrown with the given message.
   */
  static public void assertEquals(boolean actual, boolean expected, String message) {
    assertEquals(new Boolean(actual), new Boolean(expected), message);
  }

  /**
   * Asserts that two booleans are equal.
   */
  static public void assertEquals(boolean actual, boolean expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two bytes are equal. If they are not
   * an AssertionError is thrown with the given message.
   */
  static public void assertEquals(byte actual, byte expected, String message) {
    assertEquals(new Byte(actual), new Byte(expected), message);
  }

  /**
     * Asserts that two bytes are equal.
   */
  static public void assertEquals(byte actual, byte expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two chars are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(char actual, char expected, String message) {
    assertEquals(new Character(actual), new Character(expected), message);
  }

  /**
   * Asserts that two chars are equal.
   */
  static public void assertEquals(char actual, char expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two shorts are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(short actual, short expected, String message) {
    assertEquals(new Short(actual), new Short(expected), message);
  }

  /**
  * Asserts that two shorts are equal.
  */
  static public void assertEquals(short actual, short expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that two ints are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertEquals(int actual,  int expected, String message) {
    assertEquals(new Integer(actual), new Integer(expected), message);
  }

  /**
   * Asserts that two ints are equal.
  */
  static public void assertEquals(int actual, int expected) {
    assertEquals(actual, expected, null);
  }

  /**
   * Asserts that an object isn't null.
   */
  static public void assertNotNull(Object object) {
    assertNotNull(object, null);
  }

  /**
   * Asserts that an object isn't null. If it is
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertNotNull(Object object, String message) {
    assertTrue(object != null, message);
  }

  /**
   * Asserts that an object is null.
   */
  static public void assertNull(Object object) {
    assertNull(object, null);
  }

  /**
   * Asserts that an object is null.  If it is not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertNull(Object object, String message) {
    assertTrue(object == null, message);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  static public void assertSame(Object actual, Object expected, String message) {
    if(expected == actual) {
      return;
    }
    failNotSame(actual, expected, message);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionError is thrown.
   */
  static public void assertSame(Object actual, Object expected) {
    assertSame(actual, expected, null);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionError is thrown with the given message.
   */
  static public void assertNotSame(Object actual, Object expected, String message) {
    if(expected == actual) {
      failSame(actual, expected, message);
    }
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionError is thrown.
   */
  static public void assertNotSame(Object actual, Object expected) {
    assertNotSame(actual, expected, null);
  }

  static private void failSame(Object actual, Object expected, String message) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }
    fail(formatted + "expected not same with:<" + expected +"> but was same:<" + actual + ">");
  }

  static private void failNotSame(Object actual, Object expected, String message) {
    String formatted = "";
    if(message != null) {
      formatted = message + " ";
    }
    fail(formatted + "expected same with:<" + expected + "> but was:<" + actual + ">");
  }

  static private void failNotEquals(Object actual , Object expected, String message ) {
    fail(format(actual, expected, message));
  }

  static String format(Object actual, Object expected, String message) {
    String formatted = "";
    if (null != message) {
      formatted = message + " ";
    }

    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }
  
  static public void assertEquals(Collection actual, Collection expected) {
    assertEquals(actual, expected, null);
  }
  
  static public void assertEquals(Collection actual, Collection expected, String message) {
    if (null == message) message = "";
    assertEquals(actual.size(), expected.size(), message + ": lists don't have the same size");
    
    Iterator actIt = actual.iterator();
    Iterator expIt = expected.iterator();
    int i = -1;
    while(actIt.hasNext() && expIt.hasNext()) {
      i++;
      Object e = expIt.next();
      Object a = actIt.next();
      String errorMessage = message == null 
          ? "Lists differ at element [" + i + "]: " + e + " != " + a
          : message + ": Lists differ at element [" + i + "]: " + e + " != " + a;
      
      assertEquals(a, e, errorMessage);
    }
  }
  
  static public void assertEquals(Object[] actual, Object[] expected, String message) {
    if ((actual == null && expected != null) || (actual != null && expected == null)) {
      if (message != null) fail(message);
      else fail("Arrays not equal: " + expected + " and " + actual);
    }
    assertEquals(Arrays.asList(actual), Arrays.asList(expected), message);
  }
  
  /**
   * @return true if the two arrays are equal, regardless of the order of their elements
   */
  static public void assertEqualsNoOrder(Object[] actual, Object[] expected, String message) {
    if ((actual == null && expected != null) || (actual != null && expected == null)) {
      if (message != null) fail(message);
      else fail("Arrays not equal: " + expected + " and " + actual);
    }
    
    Collection actualCollection = new HashSet();
    for (Object a : actual) {
      actualCollection.add(a);
    }
    
    Collection expectedCollection = new HashSet();
    for (Object a : expected) {
      expectedCollection.add(a);
    }

    assertEquals(actualCollection, expectedCollection, message);
  }

  static public void assertEquals(Object[] actual, Object[] expected) {
    assertEquals(actual, expected, null);
  }
  
  /**
   * @return true if the two arrays are equal, regardless of the order of their elements
   */
  static public void assertEqualsNoOrder(Object[] actual, Object[] expected) {
    assertEqualsNoOrder(actual, expected, null);
  }
  
  static public void assertEquals(final byte[] actual, final byte[] expected) {
    assertEquals(actual, expected, "");
  }
  
  static public void assertEquals(final byte[] actual, final byte[] expected, final String message) {
    if(expected == actual) {
        return;
    }
    if(null == expected) {
      fail("expected a null array, but not null found. " + message);
    }
    if(null == actual) {
        fail("expected not null array, but null found. " + message);
    }
    
    assertEquals(actual.length, expected.length, "arrays don't have the same size. " + message);

    for(int i= 0; i < expected.length; i++) {
        if(expected[i] != actual[i]) {
            fail("arrays differ firstly at element [" + i +"]; "
                + "expected value is <" + expected[i] +"> but was <"
                + actual[i] + ">. "
                + message);
        }
    }
  }
}
