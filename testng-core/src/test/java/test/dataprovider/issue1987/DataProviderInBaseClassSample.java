package test.dataprovider.issue1987;

import org.testng.annotations.Test;

public class DataProviderInBaseClassSample extends BaseClassSample {

  @Test(dataProvider = "dp")
  public void testMethod(int i) {}
}
