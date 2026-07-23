package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OneDimDataProviderSample {

  @DataProvider
  public static Object[] staticArray() {
    return new Object[] {"foo", "bar"};
  }

  @Test(dataProvider = "staticArray")
  public void testStaticArray(String s) {
    assertThat(s).isNotNull();
  }

  @DataProvider
  public Object[] array() {
    return new Object[] {"foo", "bar"};
  }

  @Test(dataProvider = "array")
  public void testArray(String s) {
    assertThat(s).isNotNull();
  }

  @DataProvider
  public static Iterator<Object> staticIterator() {
    return Arrays.<Object>asList("foo", "bar").iterator();
  }

  @Test(dataProvider = "staticIterator")
  public void testStaticIterator(String s) {
    assertThat(s).isNotNull();
  }

  @DataProvider
  public Iterator<Object> iterator() {
    return Arrays.<Object>asList("foo", "bar").iterator();
  }

  @Test(dataProvider = "iterator")
  public void testIterator(String s) {
    assertThat(s).isNotNull();
  }
}
