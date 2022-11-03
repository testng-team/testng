package test.dataprovider.issue2819;

import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.Test;

public class TestClassSample {

  private int counter = 0;

  @Test(dataProvider = "dp")
  public void sampleTest(int ignored) {}

  @DataProvider(name = "dp")
  public Object[][] getTestData() {
    if (shouldSimulateFailure()) {
      throw new RuntimeException("Simulating a failure");
    }
    return new Object[][] {{1}};
  }

  private boolean shouldSimulateFailure() {
    return counter++ < 2;
  }

  public static class EnableRetryForDataProvider implements IAnnotationTransformer {
    @Override
    public void transform(IDataProviderAnnotation annotation, Method method) {
      annotation.setRetryUsing(SimpleRetry.class);
    }
  }
}
