package test.factory;

import java.util.Collections;
import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class IteratorEmptyFactorySample {

  @Factory(dataProvider = "values")
  public IteratorEmptyFactorySample(int value) {
  }

  @DataProvider(name = "values")
  public static Iterator<Object[]> values() {
    return Collections.emptyIterator();
  }

  @Test
  public void test() {
    Assert.fail();
  }
}
