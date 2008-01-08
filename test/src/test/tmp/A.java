package test.tmp;

import org.testng.annotations.Test;

public class A {

  @Test(groups = "a")
  public void f1() {
    
  }
  
  @Test(groups = "b", dependsOnGroups = "a")
  public void f2() {
    
  }
}
