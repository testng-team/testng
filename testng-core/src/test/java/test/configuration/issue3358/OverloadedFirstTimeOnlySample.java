package test.configuration.issue3358;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OverloadedFirstTimeOnlySample {
  public static final AtomicInteger befores = new AtomicInteger();

  public static void reset() {
    befores.set(0);
  }

  @BeforeMethod(firstTimeOnly = true)
  public void before() {
    befores.incrementAndGet();
  }

  @Test
  public void test() {}

  @DataProvider
  public Object[][] names() {
    return new Object[][] {{"a"}, {"b"}};
  }

  @Test(dataProvider = "names")
  public void test(String name) {}
}
