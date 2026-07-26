package test.dataprovider.issue3290;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A {@link Factory} powered by a data provider that returns a {@link Stream}. Exercises the {@code
 * FactoryMethod} consumption path and confirms the stream is closed once the factory has produced
 * its instances. {@link #CLOSE_COUNT} is asserted by the driving test.
 */
public class StreamFactoryDataProviderSample {

  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider
  public static Stream<Object[]> data() {
    return Stream.of(new Object[] {1}, new Object[] {2}).onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Factory(dataProvider = "data")
  public StreamFactoryDataProviderSample(int i) {}

  @Test
  public void testMethod() {}
}
