package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A {@code @DataProvider(parallel = true)} that returns a {@link Stream}. Confirms every row is
 * delivered when the data provider runs in parallel and that the stream is still closed exactly
 * once afterwards. {@link #CLOSE_COUNT} is asserted by the driving test.
 */
public class ParallelStreamDataProviderSample {

  public static final int ROWS = 50;
  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider(parallel = true)
  public Stream<Object[]> provide() {
    return IntStream.rangeClosed(1, ROWS)
        .mapToObj(i -> new Object[] {i})
        .onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "provide")
  public void checkParallel(Integer i) {
    assertThat(i).isNotNull();
  }
}
