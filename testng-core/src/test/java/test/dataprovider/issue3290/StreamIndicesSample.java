package test.dataprovider.issue3290;

import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Confirms that {@code @DataProvider(indices = ...)} selects the same rows from a {@link Stream} as
 * it does from an array or {@code Iterator}. Only the row at index {@code 2} (the value {@code 3})
 * should reach the test method.
 */
public class StreamIndicesSample {

  @DataProvider(indices = {2})
  public Stream<Object[]> dp() {
    return Stream.of(new Object[] {1}, new Object[] {2}, new Object[] {3});
  }

  @Test(dataProvider = "dp")
  public void indicesShouldWork(int n) {
    if (n != 3) {
      throw new RuntimeException("Only the row at index 2 (value 3) should have been selected");
    }
  }
}
