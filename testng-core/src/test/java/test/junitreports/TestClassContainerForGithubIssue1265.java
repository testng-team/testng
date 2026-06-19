package test.junitreports;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class TestClassContainerForGithubIssue1265 {
  public abstract static class ParentTest {

    @BeforeSuite
    public void startEverything() {}

    @AfterSuite
    public void shutdownContainer() {}
  }

  public static class FirstTest extends ParentTest {

    @Test
    public void should_pass() {
      assertThat("abc").isEqualTo("abc");
    }

    @Test(enabled = false)
    public void should_be_ignored() {
      assertThat("abcd").isEqualTo("abc");
    }
  }

  public static class SecondTest extends ParentTest {

    @Test
    public void should_pass_second() {
      assertThat("abc").isEqualTo("abc");
    }
  }

  public static class ThirdTest extends ParentTest {

    @Test
    public void should_pass_third() {
      assertThat("abc").isEqualTo("abc");
    }
  }
}
