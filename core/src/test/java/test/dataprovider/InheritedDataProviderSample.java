package test.dataprovider;

import org.testng.annotations.Test;

public class InheritedDataProviderSample extends InheritedDataProvider {

  @Test(dataProvider = "dp")
  public void f(String s) {}
}
