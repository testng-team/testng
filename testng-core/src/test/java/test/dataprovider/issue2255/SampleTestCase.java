package test.dataprovider.issue2255;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class SampleTestCase extends SimpleBaseTest {

  static final List<Integer> data = new ArrayList<>();
  private static final AtomicInteger counter = new AtomicInteger(0);

  @BeforeMethod
  public void beforeMethod(ITestResult result) {
    Object[] parameters = result.getParameters();
    if (parameters == null || parameters.length == 0) {
      throw new IllegalStateException("parameters aren't visible");
    }
    data.add((Integer) parameters[0]);
  }

  @Test(dataProvider = "dp")
  public void testMethod(int i) {
    int index = counter.getAndIncrement();
    assertThat(i).isEqualTo(data.get(index));
    Integer value = (Integer) Reporter.getCurrentTestResult().getParameters()[0];
    assertThat(value).isEqualTo(data.get(index));
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{100}, {200}};
  }
}
