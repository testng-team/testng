package org.testng.dependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.dependent.ClassWide1Test.m1WasRun;

import org.testng.annotations.Test;

public class ClassWide2Test {

  @Test(dependsOnMethods = {"org.testng.dependent.ClassWide1Test.m1"})
  public void m2() {
    assertThat(m1WasRun()).isTrue();
  }
}
