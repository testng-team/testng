package test.dependsongroup;

import org.testng.annotations.Configuration;


public class TestFixture1 {
  @Configuration(beforeTest=true, groups={"test", "testgroup"})
  public void setup() {
    System.out.println("TestFixture setup");
  }
  
}
