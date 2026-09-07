package org.testng.internal.classfinder.samples.github3234;

import org.testng.annotations.Test;

/**
 * An explicitly named outer class whose nested class cannot be inspected. ClassInfoMap attributes
 * that nested class to the outer {@code <class>} tag, so a missing type there must be reported.
 */
@Test
public class UnresolvedNestedTypeSample {

  public void test1() {}

  public static class Nested {

    @Test
    public void nested() {
      throw new AssertionError("should not be silently skipped");
    }

    private MissingType unused() {
      return null;
    }
  }
}
