package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A {@link Factory} powered by a data provider that returns a {@link Stream}. Exercises the {@code
 * FactoryMethod} consumption path and confirms that each stream row creates an instance with its
 * argument forwarded, and that the stream is closed once the factory has produced its instances.
 * {@link #RECEIVED} and {@link #CLOSE_COUNT} are asserted by the driving test.
 */
public class StreamFactoryDataProviderSample {

  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);
  public static final List<Integer> RECEIVED = new CopyOnWriteArrayList<>();

  private final int value;

  @DataProvider
  public static Stream<Object[]> data() {
    return Stream.of(new Object[] {1}, new Object[] {2}).onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Factory(dataProvider = "data")
  public StreamFactoryDataProviderSample(int i) {
    this.value = i;
  }

  @Test
  public void testMethod() {
    // Record the argument forwarded to this instance so the driving test can assert that every
    // stream row produced an instance with the correct value.
    RECEIVED.add(value);
    assertThat(value).isIn(1, 2);
  }
}
