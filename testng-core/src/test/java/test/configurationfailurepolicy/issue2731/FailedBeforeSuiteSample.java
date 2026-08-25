package test.configurationfailurepolicy.issue2731;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class FailedBeforeSuiteSample {

  @BeforeSuite
  public void setupSuiteFails() {
    throw new RuntimeException("setup suite fail");
  }

  @Test
  public void test1() {}
}
