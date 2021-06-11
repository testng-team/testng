package test.configuration.issue1035;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestclassExample {

  @BeforeClass
  public void beforeClass() throws InterruptedException {
    printer();
    Thread.sleep(2000);
  }

  @Test
  public void test() {}

  @AfterClass
  public void afterClass() {}

  private void printer() {
    long threadId = Thread.currentThread().getId();
    long time = System.currentTimeMillis();
    InvocationTracker tracker = new InvocationTracker(time, threadId, this);
    MyFactory.TRACKER.add(tracker);
  }
}
