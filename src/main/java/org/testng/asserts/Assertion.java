package org.testng.asserts;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An assert class with various hooks allowing its behavior to be modified
 * by subclasses.
 */
public class Assertion implements IAssertLifecycle {
  protected void doAssert(IAssert assertCommand) {
    onBeforeAssert(assertCommand);
    try {
      executeAssert(assertCommand);
      onAssertSuccess(assertCommand);
    } catch(AssertionError ex) {
      onAssertFailure(assertCommand);
    }
    onAfterAssert(assertCommand);
  }

  /**
   * Run the assert command in parameter. Meant to be overridden by subclasses.
   */
  @Override
  public void executeAssert(IAssert assertCommand) {
    assertCommand.doAssert();
  }

  /**
   * Invoked when an assert succeeds. Meant to be overridden by subclasses.
   */
  @Override
  public void onAssertSuccess(IAssert assertCommand) {
  }

  /**
   * Invoked when an assert fails. Meant to be overridden by subclasses.
   */
  @Override
  public void onAssertFailure(IAssert assertCommand) {
  }

  /**
   * Invoked before an assert is run. Meant to be overridden by subclasses.
   */
  @Override
  public void onBeforeAssert(IAssert assertCommand) {
  }

  /**
   * Invoked after an assert is run. Meant to be overridden by subclasses.
   */
  @Override
  public void onAfterAssert(IAssert assertCommand) {
  }

  abstract private static class SimpleAssert implements IAssert {
    private final String m_message;

    public SimpleAssert(String message) {
      m_message = message;
    }

    @Override
    public String getMessage() {
      return m_message;
    }

    @Override
    abstract public void doAssert();
  }

  public void assertTrue(final boolean condition, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertTrue(condition, message);
      }
    });
  }

  public void assertFalse(final boolean condition, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertFalse(condition, message);
      }
    });
  }

  public void assertFalse(final boolean condition) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertFalse(condition);
      }
    });
  }

  public void fail(final String message, final Throwable realCause) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.fail(message, realCause);
      }
    });
  }

  public void fail(final String message) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.fail(message);
      }
    });
  }

  public void fail() {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.fail();
      }
    });
  }

  public void assertEquals(final Object actual, final Object expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final String actual, final String expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }
  public void assertEquals(final String actual, final String expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final double actual, final double expected, final double delta,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, delta, message);
      }
    });
  }

  public void assertEquals(final double actual, final double expected, final double delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, delta);
      }
    });
  }

  public void assertEquals(final float actual, final float expected, final float delta,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, delta, message);
      }
    });
  }

  public void assertEquals(final float actual, final float expected, final float delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, delta);
      }
    });
  }

  public void assertEquals(final long actual, final long expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final long actual, final long expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final boolean actual, final boolean expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final boolean actual, final boolean expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final byte actual, final byte expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final byte actual, final byte expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final char actual, final char expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final char actual, final char expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final short actual, final short expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final short actual, final short expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final int actual, final  int expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final int actual, final int expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertNotNull(final Object object) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotNull(object);
      }
    });
  }

  public void assertNotNull(final Object object, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotNull(object, message);
      }
    });
  }

  public void assertNull(final Object object) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNull(object);
      }
    });
  }

  public void assertNull(final Object object, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNull(object, message);
      }
    });
  }

  public void assertSame(final Object actual, final Object expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertSame(actual, expected, message);
      }
    });
  }

  public void assertSame(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertSame(actual, expected);
      }
    });
  }

  public void assertNotSame(final Object actual, final Object expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotSame(actual, expected, message);
      }
    });
  }

  public void assertNotSame(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotSame(actual, expected);
      }
    });
  }

  public void assertEquals(final Collection<?> actual, final Collection<?> expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final Collection<?> actual, final Collection<?> expected,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final Object[] actual, final Object[] expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEqualsNoOrder(final Object[] actual, final Object[] expected,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEqualsNoOrder(actual, expected, message);
      }
    });
  }

  public void assertEquals(final Object[] actual, final Object[] expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEqualsNoOrder(final Object[] actual, final Object[] expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEqualsNoOrder(actual, expected);
      }
    });
  }

  public void assertEquals(final byte[] actual, final byte[] expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final byte[] actual, final byte[] expected,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final Set<?> actual, final Set<?> expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public void assertEquals(final Set<?> actual, final Set<?> expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }
    });
  }

  public void assertEquals(final Map<?, ?> actual, final Map<?, ?> expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }
    });
  }

  public  void assertNotEquals(final Object actual1, final Object actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  public void assertNotEquals(final Object actual1, final Object actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final String actual1, final String actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final String actual1, final String actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final long actual1, final long actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final long actual1, final long actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final boolean actual1, final boolean actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final boolean actual1, final boolean actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final byte actual1, final byte actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final byte actual1, final byte actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final char actual1, final char actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final char actual1, final char actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final short actual1, final short actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final short actual1, final short actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  void assertNotEquals(final int actual1, final int actual2, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, message);
      }
    });
  }

  void assertNotEquals(final int actual1, final int actual2) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2);
      }
    });
  }

  public void assertNotEquals(final float actual1, final float actual2, final float delta,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, delta, message);
      }
    });
  }

  public void assertNotEquals(final float actual1, final float actual2, final float delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, delta);
      }
    });
  }

  public void assertNotEquals(final double actual1, final double actual2, final double delta,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, delta, message);
      }
    });
  }

  public void assertNotEquals(final double actual1, final double actual2, final double delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual1, actual2, delta);
      }
    });
  }

}
