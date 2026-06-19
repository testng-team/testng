package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RawIteratorDataProviderSample {

  @DataProvider
  public static Iterator staticIterator() {
    return Arrays.<Object>asList(new Object[] {"foo"}, new Object[] {"bar"}).iterator();
  }

  @Test(dataProvider = "staticIterator")
  public void testStaticIterator(String s) {
    assertThat(s).isNotNull();
  }

  @DataProvider
  public Iterator iterator() {
    return Arrays.<Object>asList(new Object[] {"foo"}, new Object[] {"bar"}).iterator();
  }

  @Test(dataProvider = "iterator")
  public void testIterator(String s) {
    assertThat(s).isNotNull();
  }
}
