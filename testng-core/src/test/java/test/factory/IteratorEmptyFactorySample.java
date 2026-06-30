package test.factory;

import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.Iterator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class IteratorEmptyFactorySample {

  @DataProvider(name = "values")
  public static Iterator<Object[]> values() {
    return Collections.emptyIterator();
  }

  @Factory(dataProvider = "values")
  public IteratorEmptyFactorySample(int value) {}

  @Test
  public void test() {
    fail();
  }
}
