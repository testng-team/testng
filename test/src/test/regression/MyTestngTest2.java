package test.regression;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MyTestngTest2 extends MyTestngTest {

  @BeforeClass()
  public void beforeClass(ITestContext tc) throws Exception {
  }

  @BeforeMethod()
  public void beforeMethod(ITestContext tc) throws Exception {
      //throw new Exception("fail me");
  }
  @Test()
  public void test(ITestContext tc) {
  }
}
