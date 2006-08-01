package test.conffailure;

import org.testng.annotations.Configuration;

public class ClassWithFailedBeforeSuite {

  @Configuration(beforeSuite = true)
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

}
