package test.dataprovider.issue3290;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Proves that a {@link Stream} returned by a data provider is consumed lazily (one row at a time as
 * the test runs) rather than being drained up front. Each row records a {@code produced:} event
 * when the stream yields it and the test records a {@code consumed:} event; if consumption is lazy
 * the two interleave. {@link #EVENTS} is asserted by the driving test.
 */
public class StreamLazyLoadingDataProviderSample {

  public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

  @DataProvider
  public Stream<Object[]> data() {
    return Stream.of("a", "b", "c")
        .peek(value -> EVENTS.add("produced:" + value))
        .map(value -> new Object[] {value});
  }

  @Test(dataProvider = "data")
  public void testMethod(String value) {
    EVENTS.add("consumed:" + value);
  }
}
