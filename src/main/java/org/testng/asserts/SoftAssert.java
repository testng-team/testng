package org.testng.asserts;

import org.testng.collections.Maps;

import java.util.Map;

/**
 * When an assertion fails, don't throw an exception but record the failure.
 * Calling {@code assertAll()} will cause an exception to be thrown if at
 * least one assertion failed.
 */
public class SoftAssert extends Assertion {
  // LinkedHashMap to preserve the order
  private Map<AssertionError, IAssert> m_errors = Maps.newLinkedHashMap();

  @Override
  public void executeAssert(IAssert a) {
    try {
      a.doAssert();
    } catch(AssertionError ex) {
      m_errors.put(ex, a);
    }
  }

  public void assertAll() {
    if (! m_errors.isEmpty()) {
      StringBuilder sb = new StringBuilder("The following asserts failed:\n");
      boolean first = true;
      for (Map.Entry<AssertionError, IAssert> ae : m_errors.entrySet()) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(ae.getValue().getMessage());
      }
      throw new AssertionError(sb.toString());
    }
  }
}
