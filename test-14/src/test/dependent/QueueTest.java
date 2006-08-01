package test.dependent;

public class QueueTest { 
  /** 
   * @testng.test 
   */ 
  public void test1() { 
          System.out.println("inside test1"); 
  } 

  /** 
   * @testng.test dependsOnMethods = "test1" 
   * 
   */ 
  public void test2() { 
          System.out.println("inside test2"); 
  } 

  /** 
   * @testng.test dependsOnMethods = "test2" 
   * 
   */ 
  public void test3() { 
          System.out.println("inside test3"); 
  } 
} 

