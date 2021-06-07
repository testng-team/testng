package test.junitreports;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class TestClassContainerForGithubIssue1265 {
  public abstract static class ParentTest {

    @BeforeSuite
    public synchronized void startEverything() throws Exception {}

    @AfterSuite
    public synchronized void shutdownContainer() throws Exception {}
  }

  public static class FirstTest extends ParentTest {

    @Test
    public void should_pass() {
      assertEquals("abc", "abc");
    }

    @Test(enabled = false)
    public void should_be_ignored() {
      assertEquals("abcd", "abc");
    }
  }

  public static class SecondTest extends ParentTest {

    @Test
    public void should_pass_second() {
      assertEquals("abc", "abc");
    }
  }

  public static class ThirdTest extends ParentTest {

    @Test
    public void should_pass_third() {
      assertEquals("abc", "abc");
    }
  }
}
