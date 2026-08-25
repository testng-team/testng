package test.configurationfailurepolicy.issue2731;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailedBeforeClassSample {

  @BeforeClass
  public void setupClassFails() {
    throw new RuntimeException("setup class fail");
  }

  @Test
  public void test1() {}
}
