package test.configuration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BeforeClassThreadB {

  @BeforeClass(alwaysRun = true)
  public void setup() throws InterruptedException {
    BeforeClassParallelSupport.awaitPeer();
  }

  @Test
  public void execute() {}
}
