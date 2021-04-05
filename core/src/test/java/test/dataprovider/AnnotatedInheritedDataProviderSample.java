package test.dataprovider;

import org.testng.annotations.Test;

public class AnnotatedInheritedDataProviderSample extends AnnotatedInheritedDataProviderBaseSample {

  @Test(dataProvider = "dp")
  public void f(String a) {}
}
