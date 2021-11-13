package test.listeners.github1602;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassWithFailingConfigsSample {
  @BeforeMethod
  public void beforeMethod() {
    throw new RuntimeException();
  }

  @Test
  public void testMethod() {}

  @AfterMethod
  public void afterMethod() {
    throw new RuntimeException();
  }
}
