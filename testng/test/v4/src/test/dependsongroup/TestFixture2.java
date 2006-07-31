package test.dependsongroup;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class TestFixture2 {
  @Configuration(beforeTest=true, groups={"test"}, dependsOnGroups={"testgroup"})
  public void setup() {
    System.out.println("TestFixture2 setup");
  }

  @Test(groups={"test"})  //@@
  public void testMethod() {
    System.out.println("test method");
  }
}
