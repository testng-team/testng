package test;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestClassContainerForGitHubIssue1360 {
  public static class TestNG1 {
    @Test(priority = 1)
    public void test1TestNG1() {
      Assert.assertTrue(true);
    }

    @Test(priority = 2)
    public void test2TestNG1() {
      Assert.assertTrue(true);
    }

    @Test(priority = 3)
    public void test3TestNG1() {
      Assert.assertTrue(true);
    }
  }

  public static class TestNG2 {
    @Test(priority = 1)
    public void test1TestNG2() {
      Assert.assertTrue(true);
    }

    @Test(priority = 2)
    public void test2TestNG2() {
      Assert.assertTrue(true);
    }

    @Test(priority = 3)
    public void test3TestNG2() {
      Assert.assertTrue(true);
    }
  }

  public static class TestNG3 {
    @Test(priority = 1)
    public void test1TestNG3() {
      Assert.assertTrue(true);
    }

    @Test(priority = 2)
    public void test2TestNG3() {
      Assert.assertTrue(true);
    }

    @Test(priority = 3)
    public void test3TestNG3() {
      Assert.assertTrue(true);
    }
  }
}
