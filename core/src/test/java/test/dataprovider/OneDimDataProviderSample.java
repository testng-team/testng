package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

public class OneDimDataProviderSample {

  @DataProvider
  public static Object[] staticArray() {
    return new Object[] {"foo", "bar"};
  }

  @Test(dataProvider = "staticArray")
  public void testStaticArray(String s) {
    Assert.assertNotNull(s);
  }

  @DataProvider
  public Object[] array() {
    return new Object[] {"foo", "bar"};
  }

  @Test(dataProvider = "array")
  public void testArray(String s) {
    Assert.assertNotNull(s);
  }

  @DataProvider
  public static Iterator<Object> staticIterator() {
    return Arrays.<Object>asList("foo", "bar").iterator();
  }

  @Test(dataProvider = "staticIterator")
  public void testStaticIterator(String s) {
    Assert.assertNotNull(s);
  }

  @DataProvider
  public Iterator<Object> iterator() {
    return Arrays.<Object>asList("foo", "bar").iterator();
  }

  @Test(dataProvider = "iterator")
  public void testIterator(String s) {
    Assert.assertNotNull(s);
  }
}
