package test.listeners.issue2880;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassWithPassingConfigsSample {

  @BeforeClass
  public void beforeClass() {}

  @BeforeMethod
  public void beforeMethod() {}

  @Test
  public void testMethod() {}

  @AfterMethod
  public void afterMethod() {}

  @AfterClass
  public void afterClass() {}
}
