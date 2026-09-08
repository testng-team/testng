package org.testng.dataprovider.samples;

import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Data provider whose iterator throws from {@code hasNext()}. */
public class ThrowingHasNextDataProviderSample {

  @DataProvider
  public Iterator<Object[]> dp() {
    return new Iterator<Object[]>() {
      @Override
      public boolean hasNext() {
        throw new IllegalStateException("hasNext fails on purpose");
      }

      @Override
      public Object[] next() {
        throw new IllegalStateException("hasNext fails on purpose");
      }
    };
  }

  @Test(dataProvider = "dp")
  public void first(String value) {}

  @Test
  public void second() {}
}
