package test.priority;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SampleTest02 extends SampleTestBase {

  @Test(priority = 1)
  public void test0210_createAction() {
    assertThat(true).isTrue();
  }

  @Test(
      priority = 2,
      dependsOnMethods = {"test0210_createAction"})
  public void test0220_simpleSearch() {
    assertThat(true).isTrue();
  }

  @Test(priority = 3)
  public void test0230_advancedSearch() {
    assertThat(true).isTrue();
  }

  @Test(
      priority = 4,
      dependsOnMethods = {"test0210_createAction"})
  public void test0240_viewAction() {
    assertThat(true).isTrue();
  }

  @Test(
      priority = 5,
      dependsOnMethods = {"test0210_createAction"})
  public void test0250_modifyAction() {
    assertThat(true).isTrue();
  }

  @Test(priority = 6)
  public void test0260_deleteAction() {
    assertThat(true).isTrue();
  }
}
