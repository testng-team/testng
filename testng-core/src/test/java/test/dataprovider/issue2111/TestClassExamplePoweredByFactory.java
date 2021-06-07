package test.dataprovider.issue2111;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TestClassExamplePoweredByFactory {

  @Factory(dataProvider = "getData", dataProviderClass = DataProviderHouse.class)
  public TestClassExamplePoweredByFactory(int i) {}

  @Test
  public void test() {}
}
