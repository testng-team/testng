package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

public class RawIteratorDataProviderSample {

  @DataProvider
  public static Iterator staticIterator() {
    return Arrays.<Object>asList(new Object[] {"foo"}, new Object[] {"bar"}).iterator();
  }

  @Test(dataProvider = "staticIterator")
  public void testStaticIterator(String s) {
    Assert.assertNotNull(s);
  }

  @DataProvider
  public Iterator iterator() {
    return Arrays.<Object>asList(new Object[] {"foo"}, new Object[] {"bar"}).iterator();
  }

  @Test(dataProvider = "iterator")
  public void testIterator(String s) {
    Assert.assertNotNull(s);
  }
}
