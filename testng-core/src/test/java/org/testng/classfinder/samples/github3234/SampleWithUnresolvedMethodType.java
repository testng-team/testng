package org.testng.classfinder.samples.github3234;

import org.testng.annotations.Test;

/**
 * A test class whose unused helper names {@link MissingType} in its signature. When that type is
 * not on the class loader, reading the methods throws {@code NoClassDefFoundError}.
 */
@Test
public class SampleWithUnresolvedMethodType {

  public void test1() {
    throw new AssertionError("should not be silently skipped");
  }

  private MissingType unused() {
    return null;
  }
}
