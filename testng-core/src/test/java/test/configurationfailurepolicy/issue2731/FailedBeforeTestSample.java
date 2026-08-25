package test.configurationfailurepolicy.issue2731;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class FailedBeforeTestSample {

  @BeforeTest
  public void setupTestFails() {
    throw new RuntimeException("setup test fail");
  }

  @Test
  public void test1() {}
}
