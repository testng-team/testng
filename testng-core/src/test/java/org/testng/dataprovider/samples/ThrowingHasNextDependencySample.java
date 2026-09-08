package org.testng.dataprovider.samples;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Stream backed data provider throwing from {@code hasNext()}, on a method a failing dependency
 * already skips.
 */
public class ThrowingHasNextDependencySample {

  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @Test
  public void failing() {
    throw new IllegalStateException("this test fails on purpose");
  }

  @DataProvider
  public Stream<Object[]> dp() {
    Iterator<Object[]> throwing =
        new Iterator<Object[]>() {
          @Override
          public boolean hasNext() {
            throw new IllegalStateException("hasNext fails on purpose");
          }

          @Override
          public Object[] next() {
            throw new IllegalStateException("hasNext fails on purpose");
          }
        };
    return Stream.<Object[]>generate(throwing::next).onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "dp", dependsOnMethods = "failing")
  public void skipped(String value) {}
}
