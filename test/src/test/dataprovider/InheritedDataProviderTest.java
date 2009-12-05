package test.dataprovider;

import org.testng.annotations.Test;

//@Test(description = "class")
public class InheritedDataProviderTest extends InheritedDataProviderBaseSampleTest {

  @Test(dataProvider = "dp")
  public void f(String a) {
  }
}
