package test.conffailure;

import org.testng.annotations.BeforeSuite;

public class ClassWithFailedBeforeSuite {

  @BeforeSuite
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }

}
