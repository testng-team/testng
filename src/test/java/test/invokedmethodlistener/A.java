package test.invokedmethodlistener;

import org.testng.annotations.BeforeSuite;

public class A {

  @BeforeSuite(alwaysRun=false)
  public static void someMethod1() {}
}
