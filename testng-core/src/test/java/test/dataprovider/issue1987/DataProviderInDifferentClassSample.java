package test.dataprovider.issue1987;

import org.testng.annotations.Test;

public class DataProviderInDifferentClassSample {

  @Test(dataProvider = "dp", dataProviderClass = BaseClassSample.class)
  public void testMethod(int i) {}
}
