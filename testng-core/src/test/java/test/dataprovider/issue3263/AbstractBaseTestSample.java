package test.dataprovider.issue3263;

import org.testng.annotations.Test;

public abstract class AbstractBaseTestSample {
  @Test(dataProvider = "places")
  public void verifyPlace(String place, String city) {}
}
