package test.annotationtransformer;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class AnnotationTransformerFactorySampleTest {
  @DataProvider
  public Object[][] dataProvider() {
    return new Integer[][] {
        new Integer[] { 42 },
    };
  }

  @Factory(dataProvider = "dp")
  public Object[] init(int n) {
    return new Object[] {
        new SimpleTest(n)
    };
  }
}
