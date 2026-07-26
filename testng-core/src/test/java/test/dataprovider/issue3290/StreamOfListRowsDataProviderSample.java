package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A data provider returning {@code Stream<List<Object[]>>}. The element type is {@code
 * List<Object[]>} (not an array), so each element must be delivered as a single test parameter - it
 * must not be mistaken for a row of {@code Object[]}.
 */
public class StreamOfListRowsDataProviderSample {

  @DataProvider
  public Stream<List<Object[]>> data() {
    return Stream.of(
        Arrays.asList(new Object[] {1}, new Object[] {2}),
        Collections.singletonList(new Object[] {3}));
  }

  @Test(dataProvider = "data")
  public void testMethod(List<Object[]> row) {
    assertThat(row).isNotEmpty();
  }
}
