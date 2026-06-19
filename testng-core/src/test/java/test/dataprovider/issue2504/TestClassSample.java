package test.dataprovider.issue2504;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  @DataProvider
  public Object[][] data() {
    return new Object[][] {
      new Object[] {1}, new Object[] {2}, new Object[] {3}, new Object[] {4}, new Object[] {5}
    };
  }

  @BeforeMethod
  public void myBeforeMethod() {
    throw new RuntimeException("my exception");
  }

  @Test(dataProvider = "data")
  void testSuccess(int i) {
    assertThat(true).isTrue();
  }
}
