package test.priority;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleTest02 extends SampleTestBase {

  @Test(priority = 1)
  public void test0210_createAction() {
    Assert.assertTrue(true);
  }

  @Test(
      priority = 2,
      dependsOnMethods = {"test0210_createAction"})
  public void test0220_simpleSearch() {
    Assert.assertTrue(true);
  }

  @Test(priority = 3)
  public void test0230_advancedSearch() {
    Assert.assertTrue(true);
  }

  @Test(
      priority = 4,
      dependsOnMethods = {"test0210_createAction"})
  public void test0240_viewAction() {
    Assert.assertTrue(true);
  }

  @Test(
      priority = 5,
      dependsOnMethods = {"test0210_createAction"})
  public void test0250_modifyAction() {
    Assert.assertTrue(true);
  }

  @Test(priority = 6)
  public void test0260_deleteAction() {
    Assert.assertTrue(true);
  }
}
