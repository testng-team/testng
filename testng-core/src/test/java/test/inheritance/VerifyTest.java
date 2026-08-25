package test.inheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

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
