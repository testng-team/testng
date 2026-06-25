package test.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Test
public class Override1Sample {

  @Parameters({"InheritedFromSuite", "InheritedFromTest", "InheritedFromClass"})
  public void g(String suite, String test, String cls) {
    assertThat(suite).isEqualTo("InheritedFromSuite");
    assertThat(test).isEqualTo("InheritedFromTest");
    assertThat(cls).isEqualTo("InheritedFromClass");
  }

  public void h() {}

  @Parameters("a")
  public void f(String p) {
    assertThat(p).isEqualTo("Correct");
  }
}
