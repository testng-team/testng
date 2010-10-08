package test.configurationfailurepolicy;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeMethod {

  @BeforeMethod
  public void setupShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  @Test
  public void test1() {

  }
}
