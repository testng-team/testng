package test.configurationfailurepolicy;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeClassMethodAndAfterClass {

  @BeforeClass
  public void setupClassFails() {
    throw new RuntimeException("setup class fail");
  }

  @Test
  public void test1() {}

  @AfterClass
  public void tearDownClass() {}
}
