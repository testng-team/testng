package test.dataprovider;

import org.testng.annotations.Test;

public class StaticDataProviderSampleWithoutGuiceSample {

  @Test(dataProvider = "static", dataProviderClass = StaticProvider.class)
  public void verifyStatic(String s) {}

  @Test(dataProvider = "external", dataProviderClass = NonStaticProvider.class)
  public void verifyExternal(String s) {}
}
