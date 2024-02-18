package test.listeners.issue2916;

import org.testng.Assert;
import org.testng.annotations.*;

public class SimpleConfigTestCase {
  @BeforeSuite
  public void beforeSuite() {}

  @BeforeTest
  public void beforeTest() {}

  @BeforeClass
  public void beforeClass() {}

  @BeforeMethod
  public void beforeMethod() {
    Assert.fail();
  }

  @Test
  public void testMethod() {}
}
