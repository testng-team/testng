package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.fail;

import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** An empty {@link Stream} should simply result in no invocations, not a failure. */
public class EmptyStreamDataProviderSample {

  @DataProvider
  public Stream<Object[]> dp() {
    return Stream.empty();
  }

  @Test(dataProvider = "dp")
  public void test() {
    fail();
  }
}
