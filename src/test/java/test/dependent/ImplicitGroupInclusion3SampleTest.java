package test.dependent;

import org.testng.annotations.Test;

public class ImplicitGroupInclusion3SampleTest {
 @Test( groups = {"inc"} )
 public void test1() {}

 @Test( groups = {"exc"} )
 public void test2() {
   throw new RuntimeException("exclude me");
 }

 @Test( groups = {"exc"}, dependsOnMethods={"test2"} )
 public void test3() {
   throw new RuntimeException("exclude me");
 }

}