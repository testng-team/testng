package test.tmp;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
public class A {
  @BeforeTest(groups = {"setup"})
  public void setUp() throws Exception {
    ppp("SET UP");
  }
  
//  @Test(groups = "setup")
//  public void depended() {
//    ppp("DEPENDED UPON");
//  }

 @Test(groups = { "a", "performance-group", "regression-group" },
          dependsOnGroups = {"setup"} )
 public void dependencyTest() {
   ppp("TEST");
 }  
  private static void ppp(String s) {
    System.out.println("[A] " + s);
  }
  
//  public static EditField createField2(boolean edit) {
//    if (edit) return new EditField();
//    else return new AutoTextField();
//  }
//  
//  public static EditField createField(boolean edit) {
//    return createField2(edit) {
//      public void fixedMethod() {
//        System.out.println("Fixed method");
//      }
//    }
//  }
}

class EditField {

}

class AutoTextEditField extends EditField {
  
}

