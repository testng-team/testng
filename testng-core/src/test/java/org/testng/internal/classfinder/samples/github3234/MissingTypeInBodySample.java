package org.testng.internal.classfinder.samples.github3234;

import org.testng.annotations.Test;

/**
 * A test class that only names {@link MissingType} inside a method body. Method inspection
 * succeeds; the missing type surfaces when the test runs.
 */
@Test
public class MissingTypeInBodySample {

  public void test1() {
    new MissingType();
  }
}
