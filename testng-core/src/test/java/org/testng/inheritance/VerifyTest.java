package org.testng.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.inheritance.samples.ZBase_0;

public class VerifyTest {

  @Test(dependsOnGroups = {"before"})
  public void verify() {
    String[] expected = {
      "initApplication",
      "initDialog",
      "initDialog2",
      "test",
      "tearDownDialog2",
      "tearDownDialog",
      "tearDownApplication"
    };

    assertThat(ZBase_0.getMethodList()).containsExactly(expected);
  }
}
