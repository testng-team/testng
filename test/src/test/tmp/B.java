package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class B {

   @BeforeClass(alwaysRun = true)
   public void beforeClass() throws Exception{

       System.out.println("Before class");
   }

   @BeforeMethod(alwaysRun = true)
   public void beforeMethod(){
       System.out.println("Before method");
   }

   @Test
   public void test1(){
       System.out.println("Test 1");

   }

   @Test
   public void test2(){
       System.out.println("Test 2");

   }

   @Test
   public void test3(){
       System.out.println("Test 3");

   }


   @AfterMethod(alwaysRun = true)
   public void afterMethod(){
       System.out.println("After method");
   }

}

