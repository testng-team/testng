package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

public class FailingIterableDataProvider {

  @DataProvider(name = "dp")
  public Iterator<Object[]> createData() {
    return new Iterator<Object[]>() {
    int count=0;

    @Override
    public boolean hasNext() {
      return count<10;
    }

    @Override
    public Object[] next() {
      if (++count==6) {
        throw new RuntimeException();
      }
      return new Object[] { count };
    }

    @Override
    public void remove() {}

    };
  }

  @Test(dataProvider="dp")
  public void happyTest(int count) {
    //pass
  }
}
