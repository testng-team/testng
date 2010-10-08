package test.configurationfailurepolicy;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeMethodAndMultipleTests {

  @BeforeMethod
  public void setupShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

  @Test
  public void test1() {

  }

  @Test
  public void test2() {

  }
}
