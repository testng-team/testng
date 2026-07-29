package test.configuration.issue1035;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestclassExample {

  @BeforeClass
  public void beforeClass() throws InterruptedException {
    MyFactory.recordAndAwaitPeers();
  }

  @Test
  public void test() {}

  @AfterClass
  public void afterClass() {}
}
