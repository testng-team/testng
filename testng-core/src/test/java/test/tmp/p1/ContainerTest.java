package test.tmp.p1;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class ContainerTest {

  @BeforeSuite
  public void startup() {
    fail();
  }

  @AfterSuite
  public void shutdown() {
    fail();
  }
}
