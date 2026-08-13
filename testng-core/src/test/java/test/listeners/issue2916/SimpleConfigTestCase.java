package test.listeners.issue2916;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
