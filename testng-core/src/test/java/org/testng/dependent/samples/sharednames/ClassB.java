package org.testng.dependent.samples.sharednames;

import org.testng.annotations.Test;
import org.testng.dependent.DependsOnMethodsWithSharedNamesTest;

public class ClassB {
  @Test
  public void sameNameAA() {
    printMethod();
  }

  @Test(dependsOnMethods = "sameNameAA")
  public void uniqueNameBB() {
    printMethod();
  }

  @Test(dependsOnMethods = "uniqueNameBB")
  public void uniqueNameCC() {
    printMethod();
  }

  @Test(dependsOnMethods = "uniqueNameCC")
  public void uniqueNameDD() {
    printMethod();
  }

  @Test(dependsOnMethods = "uniqueNameDD")
  public void sameNameE() {
    printMethod();
  }

  public void nullTest() {
    printMethod();
  }

  protected void printMethod() {
    StackTraceElement[] sTrace = new Exception().getStackTrace();
    String className = sTrace[0].getClassName();
    String methodName = sTrace[1].getMethodName();

    System.out.printf("*********** executing --- %s %s\n", className, methodName);

    DependsOnMethodsWithSharedNamesTest.m_methods.add(className + "." + methodName);
  }
}
