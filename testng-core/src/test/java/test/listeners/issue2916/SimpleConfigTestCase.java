package test.listeners.issue2916;

import static org.assertj.core.api.Assertions.fail;

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
    fail();
  }

  @Test
  public void testMethod() {}
}
