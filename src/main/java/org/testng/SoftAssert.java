package org.testng;

import org.testng.collections.Maps;

import java.util.Map;

public class SoftAssert extends FlexibleAssert {
  private Map<AssertionError, IAssert> m_errors = Maps.newHashMap();

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
