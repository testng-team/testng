package test;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class TestClassContainerForGitHubIssue1360 {
  public static class TestNG1 {
    @Test(priority = 1)
    public void test1TestNG1() {
      assertThat(true).isTrue();
    }

    @Test(priority = 2)
    public void test2TestNG1() {
      assertThat(true).isTrue();
    }

    @Test(priority = 3)
    public void test3TestNG1() {
      assertThat(true).isTrue();
    }
  }

  public static class TestNG2 {
    @Test(priority = 1)
    public void test1TestNG2() {
      assertThat(true).isTrue();
    }

    @Test(priority = 2)
    public void test2TestNG2() {
      assertThat(true).isTrue();
    }

    @Test(priority = 3)
    public void test3TestNG2() {
      assertThat(true).isTrue();
    }
  }

  public static class TestNG3 {
    @Test(priority = 1)
    public void test1TestNG3() {
      assertThat(true).isTrue();
    }

    @Test(priority = 2)
    public void test2TestNG3() {
      assertThat(true).isTrue();
    }

    @Test(priority = 3)
    public void test3TestNG3() {
      assertThat(true).isTrue();
    }
  }
}
