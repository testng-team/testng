package test.annotationtransformer;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AnnotationTransformerDataProviderSampleTest {

  @DataProvider
  public Object[][] dp() {
    return new Integer[][] {
        new Integer[] { 42 },
    };
  }
  
  @Test(dataProvider = "dataProvider")
  public void f(Integer n) {
    Assert.assertEquals(n, new Integer(42));
  }
}
