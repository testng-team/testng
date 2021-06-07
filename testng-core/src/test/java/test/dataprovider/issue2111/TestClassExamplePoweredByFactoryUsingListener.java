package test.dataprovider.issue2111;

import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({LocalDataProviderInterceptor.class})
public class TestClassExamplePoweredByFactoryUsingListener {

  @Factory(dataProvider = "getData", dataProviderClass = DataProviderHouse.class)
  public TestClassExamplePoweredByFactoryUsingListener(int i) {}

  @Test
  public void test() {}
}
