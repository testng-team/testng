package org.testng.asserts;

import org.testng.collections.Maps;

import java.util.Map;

public class SoftAssert extends Assert {
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
        // TODO(cbeust): consider including the stack trace for each failure instead
        // of just the exception message
        sb.append(ae.getValue().getMessage());
      }
      throw new AssertionError(sb.toString());
    }
  }
}
