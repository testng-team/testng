package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ClassWide2Test {

  @Test(dependsOnMethods = {"test.dependent.ClassWide1Test.m1"})
  public void m2() {
    assertThat(ClassWide1Test.m1WasRun()).isTrue();
  }
}
