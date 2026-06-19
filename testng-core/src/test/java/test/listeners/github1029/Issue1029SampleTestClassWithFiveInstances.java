package test.listeners.github1029;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class Issue1029SampleTestClassWithFiveInstances {

  private final int i;

  @Factory(dataProvider = "dp")
  public Issue1029SampleTestClassWithFiveInstances(int i) {
    this.i = i;
  }

  @Test
  public void a() {
    assertThat(i).isPositive();
  }

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {{1}, {2}, {3}, {4}, {5}};
  }
}
