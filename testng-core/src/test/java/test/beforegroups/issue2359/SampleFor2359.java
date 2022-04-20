package test.beforegroups.issue2359;

import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class SampleFor2359 {

  @BeforeGroups(groups = "testng2359")
  public void beforeGroup() {
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test(groups = "testng2359")
  public void sampleTest1() {}

  @Test(groups = "testng2359")
  public void sampleTest2() {}

  @Test(groups = "testng2359")
  public void sampleTest3() {}
}
