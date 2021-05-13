package test.regression;

import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public class MyTestngTest {

  @BeforeSuite()
  public void beforeSuite(ITestContext tc) throws Exception {
  }

  @BeforeTest()
  public void beforeTest(ITestContext tc) throws Exception {
      throw new RuntimeException("barfing now");
  }
}
