package test.github1362;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class TestSample {

  @BeforeGroups(groups = {"exTests"})
  public void setup() {}

  @Test(groups = {"exTests"})
  public void test1() {
    Assert.assertTrue(true, "test1");
  }

  @Test(groups = {"exTests"})
  public void test2() {
    Assert.assertTrue(true, "test2");
  }

  @Test(groups = {"exTests"})
  public void test3() {
    Assert.assertTrue(true, "test3");
  }

  @AfterGroups(groups = {"exTests"})
  public void clear() {}
}
