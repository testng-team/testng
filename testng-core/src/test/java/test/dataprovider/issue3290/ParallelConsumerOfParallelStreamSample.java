package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A parallel data-driven test ({@code @DataProvider(parallel = true)}) whose data provider also
 * returns a <em>parallel</em> {@link Stream}. TestNG pulls rows from the stream's iterator under
 * its own synchronization while dispatching invocations across threads, so every row should still
 * be delivered exactly once and the stream closed once. {@link #CLOSE_COUNT} is asserted by the
 * driving test.
 */
public class ParallelConsumerOfParallelStreamSample {

  public static final int ROWS = 50;
  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider(parallel = true)
  public Stream<Object[]> provide() {
    return IntStream.rangeClosed(1, ROWS)
        .parallel()
        .mapToObj(i -> new Object[] {i})
        .onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "provide")
  public void test(Integer i) {
    assertThat(i).isNotNull();
  }
}
