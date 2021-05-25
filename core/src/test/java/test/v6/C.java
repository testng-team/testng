package test.v6;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class C {

  @BeforeTest
  public void beforeTest() {}

  @AfterTest
  public void afterTest() {}

  @Test
  public void fc1() {}

  @BeforeSuite
  public void beforeSuite() {}

  @AfterSuite
  public void afterSuite() {}

}
