package test.dataprovider.issue2111;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({LocalDataProviderInterceptor.class})
public class TestClassSampleUsingListener {

  @Test(dataProvider = "getData", dataProviderClass = DataProviderHouse.class)
  public void test(int i) {}
}
