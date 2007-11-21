package test.invocationcount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class InvocationCountTrueFalseTest extends InvocationBase {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {
    incrementBefore();
  }
  
  @AfterMethod(lastTimeOnly = false)
  public void afterMethod() {
    incrementAfter();
  }
}
