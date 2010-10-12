package test.configurationfailurepolicy;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExtendsClassWithFailedBeforeMethod extends ClassWithFailedBeforeMethod {

  @BeforeMethod
  public void setupExtension() {

  }

  @Test
  public void test2() {

  }
}
