package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Verifies that a {@link Stream} returned by a data provider has its {@link Stream#close()} invoked
 * once the rows have been consumed. The {@link #CLOSE_COUNT} is asserted by the driving test after
 * the run completes.
 */
public class StreamClosingDataProviderSample {

  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider
  public Stream<Object[]> data() {
    return Stream.of(new Object[] {"Jack", 5}, new Object[] {"Joe", 10})
        .onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "data")
  public void testMethod(String name, int age) {
    assertThat(name).isNotNull();
    assertThat(age).isPositive();
  }
}
