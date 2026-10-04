package org.testng.configuration.test111;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.configuration.samples.test111.AbstractTest;

public class Test1 extends AbstractTest {
  @Test(description = "GITHUB-111")
  public void test() {
    assertThat(AbstractTest.R)
        .withFailMessage("the parent's @AfterClass ran before this test method")
        .isZero();
  }
}
