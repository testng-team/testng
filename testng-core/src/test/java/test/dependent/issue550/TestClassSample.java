package test.dependent.issue550;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test(dependsOnMethods = "a")
  public void b() {}

  @BeforeMethod
  public void a() {}
}
