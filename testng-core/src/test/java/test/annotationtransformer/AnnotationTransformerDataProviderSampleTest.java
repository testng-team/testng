package test.annotationtransformer;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AnnotationTransformerDataProviderSampleTest {

  @DataProvider
  public Object[][] dp() {
    return new Integer[][] {
      new Integer[] {42},
    };
  }

  @Test(dataProvider = "dataProvider")
  public void f(Integer n) {
    assertThat(n).isEqualTo(Integer.valueOf(42));
  }
}
