package test.invokedmethodlistener;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

@Test
public class C extends A{

  @BeforeSuite
  public static void someMethod3() {}

  public void someTest() {}
}
