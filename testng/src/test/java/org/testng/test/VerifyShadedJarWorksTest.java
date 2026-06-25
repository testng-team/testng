package org.testng.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

/**
 * The test does not look sophisticated, however it still verifies that testng-all.jar works to a
 * certain degree.
 */
public class VerifyShadedJarWorksTest {
  @Test
  public void testHelloWorld() {
    assertThat(2 + 2).as("2 + 2").isEqualTo(4);
  }
}
