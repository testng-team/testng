package test.dataprovider;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FailingDataProviderSample {

  @DataProvider
  public Object[][] throwsExpectedException() {
    throw new RuntimeException("expected exception from @DP");
  }

  @Test(dataProvider = "throwsExpectedException")
  public void dpThrowingException() {
    fail("Method should never get invoked");
  }
}
