package test.tmp;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test(sequential=true)
public class A {
  @Test(groups = "yyy")
  public void f() {}
  
       @Test (enabled=true, groups={"xxx"}, dependsOnGroups={"yyy"})
       public void testMethodA() throws Exception {
       // 
       }

       @AfterClass(enabled=true,groups={"xxx"})
       public void tearDown() throws Exception {
       //
       }
}
