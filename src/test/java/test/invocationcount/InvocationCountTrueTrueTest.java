package test.invocationcount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class InvocationCountTrueTrueTest extends InvocationBase {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterMethod() {}
}
