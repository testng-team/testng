package test.beforegroups.issue118;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Test(groups = "group1")
public class TestclassSample {
  private Object testObject;

  @BeforeGroups(inheritGroups = true)
  public void setUpGroup() {
    testObject = new Object();
  }

  public void test1() {
    assertThat(testObject)
        .withFailMessage("@BeforeGroups not invoked if nothing explicitly specified")
        .isNotNull();
  }
}
