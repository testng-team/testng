package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A raw (unparameterized) {@link Stream} return type should behave like a raw {@code Iterator} does
 * today - the user is responsible for providing rows in the shape the test method expects.
 */
public class RawStreamDataProviderSample {

  @DataProvider
  public static Stream staticStream() {
    return Stream.of(new Object[] {"foo"}, new Object[] {"bar"});
  }

  @Test(dataProvider = "staticStream")
  public void testStaticStream(String s) {
    assertThat(s).isNotNull();
  }

  @DataProvider
  public Stream stream() {
    return Stream.of(new Object[] {"foo"}, new Object[] {"bar"});
  }

  @Test(dataProvider = "stream")
  public void testStream(String s) {
    assertThat(s).isNotNull();
  }
}
