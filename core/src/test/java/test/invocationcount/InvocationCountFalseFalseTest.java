package test.invocationcount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class InvocationCountFalseFalseTest extends InvocationBase {
  @BeforeMethod(firstTimeOnly = false)
  public void beforeMethod() {}

  @AfterMethod(lastTimeOnly = false)
  public void afterMethod() {}
}
