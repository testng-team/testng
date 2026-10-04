package org.testng.configuration.test111;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.configuration.samples.test111.AbstractTest;

public class Test1 extends AbstractTest {

  /**
   * Sets the counter back to zero. It is static and nothing else resets it, so a second run of this
   * class in one JVM would read what the first run's {@code @AfterClass} left behind, and the
   * assertion below would then fail while saying something untrue.
   */
  @BeforeClass
  public void resetTheCounter() {
    AbstractTest.R = 0;
  }

  @Test(description = "GITHUB-111")
  public void test() {
    assertThat(AbstractTest.R)
        .withFailMessage("the parent's @AfterClass ran before this test method")
        .isZero();
  }
}
