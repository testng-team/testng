package test.priority;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleTest01 extends SampleTestBase {
  @Test(priority = 1)
  public void test0010_createAction() {
    throw new RuntimeException("Simulating failure");
  }

  @Test(
      priority = 2,
      dependsOnMethods = {"test0010_createAction"})
  public void test0020_simpleSearch() {
    Assert.assertTrue(true);
  }

  @Test(priority = 3)
  public void test0030_advancedSearch() {
    Assert.assertTrue(true);
  }

  @Test(
      priority = 4,
      dependsOnMethods = {"test0010_createAction"})
  public void test0040_viewAction() {
    Assert.assertTrue(true);
  }

  @Test(
      priority = 5,
      dependsOnMethods = {"test0010_createAction"})
  public void test0050_modifyAction() {
    Assert.assertTrue(true);
  }

  @Test(priority = 6)
  public void test0060_deleteAction() {
    Assert.assertTrue(true);
  }
}
