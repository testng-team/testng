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
      onAssertFailure(assertCommand, ex);
      throw ex;
    } finally {
      onAfterAssert(assertCommand);
    }
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
   * 
   * @deprecated use onAssertFailure(IAssert assertCommand, AssertionError ex) instead of.
   */
  @Deprecated
  @Override
  public void onAssertFailure(IAssert assertCommand) {
  }
  
  @Override
  public void onAssertFailure(IAssert assertCommand, AssertionError ex) {
      onAssertFailure(assertCommand);
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
    public Object getActual() {
        return null;
    }

    @Override
    public Object getExpected() {
        return null;
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

      @Override
      public Object getActual() {
        return condition;
      }

      @Override
      public Object getExpected() {
        return true;
      }
    });
  }
  
	public void assertTrue(final boolean condition) {
		doAssert(new SimpleAssert(null) {
			@Override
			public void doAssert() {
				org.testng.Assert.assertTrue(condition);
			}

			@Override
			public Object getActual() {
				return condition;
			}

			@Override
			public Object getExpected() {
				return true;
			}
		});
	}

  public void assertFalse(final boolean condition, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertFalse(condition, message);
      }

      @Override
      public Object getActual() {
        return condition;
      }

      @Override
      public Object getExpected() {
        return false;
      }
    });
  }

  public void assertFalse(final boolean condition) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertFalse(condition);
      }

      @Override
      public Object getActual() {
        return condition;
      }

      @Override
      public Object getExpected() {
        return false;
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
    doAssert(new SimpleAssert(message) {
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

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final String actual, final String expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }
  public void assertEquals(final String actual, final String expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
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

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final double actual, final double expected, final double delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, delta);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
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

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final float actual, final float expected, final float delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, delta);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final long actual, final long expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final long actual, final long expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final boolean actual, final boolean expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final boolean actual, final boolean expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final byte actual, final byte expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final byte actual, final byte expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final char actual, final char expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final char actual, final char expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final short actual, final short expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final short actual, final short expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final int actual, final  int expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final int actual, final int expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotNull(final Object object) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotNull(object);
      }

      @Override
      public Object getActual() {
        return object;
      }
    });
  }

  public void assertNotNull(final Object object, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotNull(object, message);
      }

      @Override
      public Object getActual() {
        return object;
      }
    });
  }

  public void assertNull(final Object object) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNull(object);
      }

      @Override
      public Object getActual() {
        return object;
      }
    });
  }

  public void assertNull(final Object object, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNull(object, message);
      }

      @Override
      public Object getActual() {
        return object;
      }
    });
  }

  public void assertSame(final Object actual, final Object expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertSame(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertSame(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertSame(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotSame(final Object actual, final Object expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotSame(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotSame(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotSame(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Collection<?> actual, final Collection<?> expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
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

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Object[] actual, final Object[] expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
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

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Object[] actual, final Object[] expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEqualsNoOrder(final Object[] actual, final Object[] expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEqualsNoOrder(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final byte[] actual, final byte[] expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
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

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Set<?> actual, final Set<?> expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Set<?> actual, final Set<?> expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertEquals(final Map<?, ?> actual, final Map<?, ?> expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public  void assertNotEquals(final Object actual, final Object expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotEquals(final Object actual, final Object expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final String actual, final String expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final String actual, final String expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final long actual, final long expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final long actual, final long expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final boolean actual, final boolean expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final boolean actual, final boolean expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final byte actual, final byte expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final byte actual, final byte expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final char actual, final char expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final char actual, final char expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final short actual, final short expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final short actual, final short expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final int actual, final int expected, final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  void assertNotEquals(final int actual, final int expected) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotEquals(final float actual, final float expected, final float delta,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, delta, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
   });
  }

  public void assertNotEquals(final float actual, final float expected, final float delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, delta);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotEquals(final double actual, final double expected, final double delta,
      final String message) {
    doAssert(new SimpleAssert(message) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, delta, message);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

  public void assertNotEquals(final double actual, final double expected, final double delta) {
    doAssert(new SimpleAssert(null) {
      @Override
      public void doAssert() {
        org.testng.Assert.assertNotEquals(actual, expected, delta);
      }

      @Override
      public Object getActual() {
          return actual;
      }

      @Override
      public Object getExpected() {
          return expected;
      }
    });
  }

}
