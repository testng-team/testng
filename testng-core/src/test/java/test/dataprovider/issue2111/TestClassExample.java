package test.dataprovider.issue2111;

import org.testng.annotations.Test;

public class TestClassExample {

  @Test(dataProvider = "getData", dataProviderClass = DataProviderHouse.class)
  public void test(int i) {}
}
