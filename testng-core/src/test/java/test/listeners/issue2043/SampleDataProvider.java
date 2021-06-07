package test.listeners.issue2043;

import org.testng.annotations.DataProvider;

public class SampleDataProvider {

  @DataProvider
  public Object[][] dp1master() {
    return new Object[][] {new Object[] {Object.class}, {Object.class}};
  }

  @DataProvider
  public Object[][] dp2master() {
    return new Object[][] {new Object[] {Object.class}, {Object.class}};
  }
}
