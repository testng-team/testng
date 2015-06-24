package test.invocationcount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class InvocationCountFalseTrueTest extends InvocationBase {
  @BeforeMethod(firstTimeOnly = false)
  public void beforeMethod() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterMethod() {}
}
