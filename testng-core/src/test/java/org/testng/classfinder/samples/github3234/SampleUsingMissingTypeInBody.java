package org.testng.classfinder.samples.github3234;

import org.testng.annotations.Test;

/**
 * A test class that only names {@link MissingType} inside a method body. Method inspection
 * succeeds; the missing type surfaces when the test runs.
 */
@Test
public class SampleUsingMissingTypeInBody {

  public void test1() {
    new MissingType();
  }
}
