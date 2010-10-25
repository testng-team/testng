package test.configurationfailurepolicy;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeClassMethod {

  @BeforeClass
  public void setupClassFails() {
    throw new RuntimeException( "setup class fail" );
  }

  @BeforeMethod
  public void setupMethod() {

  }

  @Test
  public void test1() {

  }
}
