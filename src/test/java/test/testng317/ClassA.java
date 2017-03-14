package test.testng317;

import org.testng.annotations.Test;


public class ClassA {
  @Test
  public void sameNameA(){
    printMethod();
  }
  @Test (dependsOnMethods="sameNameA")
  public void uniqueNameB(){
    printMethod();
  }
  @Test (dependsOnMethods="uniqueNameB")
  public void uniqueNameC(){
    printMethod();
  }
  @Test (dependsOnMethods="uniqueNameC")
  public void uniqueNameD(){
    printMethod();
  }
  @Test (dependsOnMethods="uniqueNameD")
  public void sameNameE(){
    printMethod();
  }
  @Test (dependsOnMethods="sameNameE")
  public void sameNameF(){
    printMethod();
  }
  @Test (dependsOnMethods="sameNameF")
  public void sameNameG(){
    printMethod();
  }
  @Test (dependsOnMethods="sameNameG")
  public void sameNameH(){
    printMethod();
  }

  public void nullTest(){
    printMethod();
  }
  protected void printMethod() {
    StackTraceElement[] sTrace = new Exception().getStackTrace();
    String className = sTrace[0].getClassName();
    String methodName = sTrace[1].getMethodName();

    System.out.printf("*********** executing --- %s %s\n", className, methodName);

    VerifyTest.m_methods.add(className + "." + methodName);
  }
}
