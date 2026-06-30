package test.listeners.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleTestClassWithDataDrivenMethodPassAndFailedIterations {

  @Test(dataProvider = "dp")
  public void testWillPass(int i) {
    if (i == 1) {
      fail();
    }
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
