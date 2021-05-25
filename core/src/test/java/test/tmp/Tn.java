package test.tmp;

import org.testng.Reporter;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class Tn {
  @Test (groups = {"g1"})
  public void m1() {
  System.out.println("1");
 }

 @Test (groups = {"g1"}, dependsOnMethods="m1")
  public void m2() {
  System.out.println("2");
 }

 @Parameters(value = "param")
 @Test (groups = {"g2"})
  public void m3(String param) {
  System.out.println("3");
  Reporter.log("M3 WAS CALLED");
 }
}
