package test.thread.issue2019;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  public static Queue<Long> threads = new ConcurrentLinkedQueue<>();

  @Test
  public void testA() {}

  @Test
  public void testB() {}

  @Test(dataProvider = "getTestData")
  public void testC(boolean ignored) {}

  @Test(dataProvider = "getTestData")
  public void testD(boolean ignored) {}

  @AfterMethod
  public void log() {
    threads.add(Thread.currentThread().getId());
  }

  @DataProvider(name = "getTestData", parallel = true)
  public Object[][] getTestData() {
    return new Object[][] {{true}, {false}, {true}, {false}};
  }
}
