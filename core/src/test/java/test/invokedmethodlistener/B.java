package test.invokedmethodlistener;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test(enabled=false)
public class B extends A{

  @BeforeSuite
  public static void someMethod2() {}

  public void someTest() {}
}
