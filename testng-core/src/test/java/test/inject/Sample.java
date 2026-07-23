package test.inject;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;

public class Sample {

  @Test
  public void f(ITestContext tc) {
    assertThat(tc).isNotNull();
    ITestNGMethod[] allMethods = tc.getAllTestMethods();
    assertThat(allMethods.length).isEqualTo(1);
    assertThat(allMethods[0].getConstructorOrMethod().getName()).isEqualTo("f");
  }
}
