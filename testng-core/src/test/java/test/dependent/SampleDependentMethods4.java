package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SampleDependentMethods4 {

  @Test
  public void step1() {}

  @Test(dependsOnMethods = {"step1"})
  public void step2() {
    assertThat(false).withFailMessage("Problem in step2").isTrue();
  }

  @Test(dependsOnMethods = {"step2"})
  public void step3() {}
}
