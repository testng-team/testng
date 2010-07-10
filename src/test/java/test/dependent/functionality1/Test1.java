package test.dependent.functionality1;

import org.testng.annotations.Test;

@Test (groups = "tests.functional.package")
public class Test1 {

   public void test1_1(){
       System.out.println("Test 1_1");
   }
   public void test1_2(){
       System.out.println("Test 1_2");
   }
}
