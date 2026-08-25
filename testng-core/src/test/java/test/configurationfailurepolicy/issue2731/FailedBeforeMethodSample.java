package test.configurationfailurepolicy.issue2731;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FailedBeforeMethodSample {

  @BeforeMethod
  public void setupMethodFails() {
    throw new RuntimeException("setup method fail");
  }

  @Test
  public void test1() {}
}
