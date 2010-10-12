package test.configurationfailurepolicy;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ClassWithSkippingBeforeMethod {

  private int invocations = 0;

  @BeforeMethod
  public void beforeMethod() {
      invocations++;
      if (invocations ==2) {
        throw new SkipException("skipping");
      }
  }

  @Test
  public void test1() {}

  @Test
  public void test2() {}
}
