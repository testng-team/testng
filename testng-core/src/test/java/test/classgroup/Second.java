package test.classgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static test.classgroup.First.allRun;

import org.testng.annotations.Test;

@Test(dependsOnGroups = {"first"})
public class Second {

  @Test
  public void verify() {
    assertThat(allRun())
        .withFailMessage("Methods for class First should have been invoked first.")
        .isTrue();
  }
}
