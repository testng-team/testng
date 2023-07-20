package test.dataprovider.issue2934;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.Test;

public class TestCaseSample {

  @Test(dataProvider = "testData", retryAnalyzer = RetryAnalyzer.class)
  public void API_dummyTest(int number) {
    assertThat(number % 2).isEqualTo(0);
  }

  @DataProvider(name = "testData")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}, {3}};
  }

  public static class ToggleDataProvider implements IAnnotationTransformer {

    private final boolean isParallel;

    public ToggleDataProvider(boolean isParallel) {
      this.isParallel = isParallel;
    }

    @Override
    public void transform(IDataProviderAnnotation annotation, Method method) {
      if (isParallel) {
        annotation.setParallel(true);
      }
    }
  }

  public static class RetryAnalyzer implements IRetryAnalyzer {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public boolean retry(ITestResult iTestResult) {
      return counter.getAndIncrement() < 3;
    }
  }

  public static class CoreListener implements ITestListener {

    private ITestContext context;

    @Override
    public void onFinish(ITestContext context) {
      this.context = context;
    }

    public int totalTests() {
      return context.getPassedTests().size()
          + context.getFailedTests().size()
          + context.getSkippedTests().size();
    }

    public Set<ITestResult> getPassedTests() {
      return context.getPassedTests().getAllResults();
    }

    public Set<ITestResult> getFailedTests() {
      return context.getFailedTests().getAllResults();
    }

    public Set<ITestResult> getSkippedTests() {
      return context.getSkippedTests().getAllResults();
    }
  }
}
