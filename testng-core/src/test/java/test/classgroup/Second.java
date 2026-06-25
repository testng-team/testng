package test.classgroup;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test(dependsOnGroups = {"first"})
public class Second {

  @Test
  public void verify() {
    assertThat(First.allRun())
        .withFailMessage("Methods for class First should have been invoked first.")
        .isTrue();
  }
}
