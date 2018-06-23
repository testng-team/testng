package org.testng.asserts;

import java.util.Map;

import org.testng.collections.Maps;

/**
 * When an assertion fails, don't throw an exception but record the failure. Calling {@code
 * assertAll()} will cause an exception to be thrown if at least one assertion failed.
 */
public class SoftAssert extends Assertion {
  // LinkedHashMap to preserve the order
  private final Map<AssertionError, IAssert<?>> m_errors = Maps.newLinkedHashMap();

  @Override
  protected void doAssert(IAssert<?> a) {
    onBeforeAssert(a);
    try {
      a.doAssert();
      onAssertSuccess(a);
    } catch (AssertionError ex) {
      onAssertFailure(a, ex);
      m_errors.put(ex, a);
    } finally {
      onAfterAssert(a);
    }
  }

  public void assertAll() {
    if (!m_errors.isEmpty()) {
      StringBuilder sb = new StringBuilder("The following asserts failed:");
      boolean first = true;
      for (AssertionError error : m_errors.keySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        sb.append("\n\t");
        sb.append(error.getMessage());
        Throwable cause = error.getCause();
        while (cause != null) {
          sb.append(" ").append(cause.getMessage());
          cause = cause.getCause();
        }
      }
      throw new AssertionError(sb.toString());
    }
  }
}
