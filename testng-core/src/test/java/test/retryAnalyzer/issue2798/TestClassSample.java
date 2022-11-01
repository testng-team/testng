package test.retryAnalyzer.issue2798;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassSample {

  @DataProvider(name = "test data", parallel = true)
  public Iterator<Object[]> data() {
    return Arrays.asList(new Object[] {"1"}, new Object[] {"2"}).iterator();
  }

  @Test(dataProvider = "test data", retryAnalyzer = HashCodeAwareRetryAnalyzer.class)
  public void foo(String ignored) throws IOException {
    throw new FileNotFoundException("this can be retried");
  }
}
