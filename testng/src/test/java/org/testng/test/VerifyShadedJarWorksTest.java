package org.testng.test;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The test does not look sophisticated, however it still verifies that testng-all.jar works to a
 * certain degree.
 */
public class VerifyShadedJarWorksTest {
  @Test
  public void testHelloWorld() {
    Assert.assertEquals(4, 2 + 2, "2 + 2");
  }
}
