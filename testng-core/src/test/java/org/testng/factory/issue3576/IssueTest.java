package org.testng.factory.issue3576;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.factory.samples.issue3576.PooledFactorySample;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  /**
   * Each invocation of a method with a {@code threadPoolSize} greater than 1 runs on a copy of the
   * method. The copy must keep the parameters that the {@code @Factory} gave to its instance.
   */
  @Test(description = "GITHUB-3576", dataProvider = "lazy")
  public void pooledInvocationsKeepTheirFactoryParameters(boolean lazy) {
    TestNG testng = create(PooledFactorySample.class);
    testng.setLazyFactoryInstantiation(lazy);
    FactoryParameters listener = new FactoryParameters();
    testng.addListener(listener);
    testng.run();

    assertThat(testng.getStatus()).isZero();
    assertThat(listener.results)
        .containsExactlyInAnyOrder(
            "serial [alpha] alpha",
            "serial [alpha] alpha",
            "serial [beta] beta",
            "serial [beta] beta",
            "pooled [alpha] alpha",
            "pooled [alpha] alpha",
            "pooled [beta] beta",
            "pooled [beta] beta");
  }

  @DataProvider(name = "lazy")
  public Object[][] lazy() {
    return new Object[][] {{false}, {true}};
  }

  public static class FactoryParameters implements ITestListener {

    private final List<String> results = new CopyOnWriteArrayList<>();

    @Override
    public void onTestSuccess(ITestResult result) {
      results.add(
          result.getMethod().getMethodName()
              + " "
              + Arrays.toString(result.getFactoryParameters())
              + " "
              + result.getInstance());
    }
  }
}
