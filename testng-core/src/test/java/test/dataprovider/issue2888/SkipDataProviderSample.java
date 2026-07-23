package test.dataprovider.issue2888;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({SkipDataProviderListener.class})
public class SkipDataProviderSample {
  @Test(groups = "SkipMe")
  public void testSkip() {
    fail("This test should not execute, it should be skipped");
  }

  @DataProvider(name = "dataProvider")
  private Object[][] dataProvider() {
    return new Object[][] {new Object[] {"test1"}};
  }

  @Test(dataProvider = "dataProvider", groups = "SkipMe")
  public void testSkipWithDataProvider(String a) {
    fail("This test should not execute, it should be skipped");
  }
}
