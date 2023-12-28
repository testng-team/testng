package test.dataprovider;

import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FailingIterableDataProvider {

  @DataProvider(name = "dp")
  public Iterator<Object[]> createData() {
    return new Iterator<>() {
      int count = 0;

      @Override
      public boolean hasNext() {
        return count < 10;
      }

      @Override
      public Object[] next() {
        if (++count == 6) {
          throw new RuntimeException();
        }
        return new Object[] {count};
      }
    };
  }

  @Test(dataProvider = "dp")
  public void happyTest(int count) {
    // pass
  }
}
