package test.dataprovider.issue3290;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StreamDataProviderSample {

  @DataProvider
  public static Stream<Object[]> staticStream() {
    return Stream.of(new Object[] {"Jack", 5}, new Object[] {"Joe", 10});
  }

  @Test(dataProvider = "staticStream")
  public void testStaticStream(String name, int age) {
    assertThat(name).isNotNull();
    assertThat(age).isPositive();
  }

  @DataProvider
  public Stream<Object[]> stream() {
    return Stream.of(new Object[] {"Jack", 5}, new Object[] {"Joe", 10});
  }

  @Test(dataProvider = "stream")
  public void testStream(String name, int age) {
    assertThat(name).isNotNull();
    assertThat(age).isPositive();
  }

  @DataProvider
  public static Stream<Object> staticOneDimStream() {
    return Stream.of("foo", "bar");
  }

  @Test(dataProvider = "staticOneDimStream")
  public void testStaticOneDimStream(String s) {
    assertThat(s).isNotNull();
  }

  @DataProvider
  public Stream<Object> oneDimStream() {
    return Stream.of("foo", "bar");
  }

  @Test(dataProvider = "oneDimStream")
  public void testOneDimStream(String s) {
    assertThat(s).isNotNull();
  }
}
