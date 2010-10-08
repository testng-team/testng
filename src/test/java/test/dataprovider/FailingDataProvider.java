package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 * This class/interface
 */
public class FailingDataProvider {
  @DataProvider
  public Object[][] throwsExpectedException() {
    throw new RuntimeException("expected exception from @DP");
  }

  @Test(dataProvider="throwsExpectedException")
  public void dpThrowingException() {
    Assert.fail("Method should never get invoked");
  }
}
