package test.conffailure;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeTestClass {
  @Configuration(beforeTestClass = true)
  public void setUpShouldFail() {
    throw new RuntimeException("Failing in setUp");
  }
  
  // Adding this method or @Configuration will never be invoked
  @Test
  public void dummy() {
    
  }
  
}
