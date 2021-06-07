package test.thread.parallelization.issue2321;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestMultipleInstance {

  private long threadId;

  @Factory(dataProvider = "dp")
  public TestMultipleInstance(int part) {}

  @Test
  public void independent() {
    threadId = Thread.currentThread().getId();
  }

  @Test(dependsOnMethods = "independent")
  public void dependent() {
    long currentThreadId = Thread.currentThread().getId();
    Assert.assertEquals(currentThreadId, threadId, "Thread Ids didn't match");
  }

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
