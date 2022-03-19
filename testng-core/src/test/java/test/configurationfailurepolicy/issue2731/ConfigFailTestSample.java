package test.configurationfailurepolicy.issue2731;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ConfigFailTestSample {
  @BeforeSuite
  public void beforeSuite() {
    Assert.fail("This before suite is fail");
  }

  @BeforeTest
  public void beforeTest() {
    Assert.fail("This before test is fail");
  }

  @BeforeClass
  public void beforeClass() {
    Assert.fail("This before class is fail");
  }

  @BeforeMethod
  public void beforeMethod() {
    Assert.fail("This before method is fail");
  }

  @Test
  public void test() {}
}
