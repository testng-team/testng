package test.factory;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Iterator;

public class IteratorEmptyFactorySample {

  @DataProvider(name = "values")
  public static Iterator<Object[]> values() {
    return Collections.emptyIterator();
  }

  @Factory(dataProvider = "values")
  public IteratorEmptyFactorySample(int value) {}

  @Test
  public void test() {
    Assert.fail();
  }
}
