package test.factory.lazy;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * A constructor factory fed by a DataProvider whose {@link Iterator} hands back the <em>same</em>
 * {@code Object[]} instance for every row (a reused buffer), mutating it in place on each {@code
 * next()}. This is legal per the {@code Iterator<Object[]>} contract.
 *
 * <p>Eager construction consumes each row immediately, so the reuse is invisible. Lazy construction
 * retains the row until the instance instantiates later, so unless the factory snapshots each row
 * every lazy instance would end up sharing the buffer and observe only the last value. Each test
 * records the value its own instance was built with; a correct (snapshotting) implementation yields
 * the four distinct values.
 */
public class ReusedBufferFactorySample {

  public static final List<Integer> VALUES_SEEN = new CopyOnWriteArrayList<>();

  private final int value;

  @Factory(dataProvider = "reusedBuffer")
  public ReusedBufferFactorySample(int value) {
    this.value = value;
  }

  @DataProvider
  public static Iterator<Object[]> reusedBuffer() {
    return new Iterator<Object[]>() {
      private final Object[] buffer = new Object[1];
      private int next = 0;

      @Override
      public boolean hasNext() {
        return next < 4;
      }

      @Override
      public Object[] next() {
        buffer[0] = next++;
        return buffer; // deliberately the same array instance on every call
      }
    };
  }

  @Test
  public void test() {
    VALUES_SEEN.add(value);
  }

  public static void reset() {
    VALUES_SEEN.clear();
  }
}
