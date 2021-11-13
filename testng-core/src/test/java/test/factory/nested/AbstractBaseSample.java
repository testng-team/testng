package test.factory.nested;

import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractBaseSample {

  protected abstract String someMethod(String param);

  @Test
  public void test() {
    String result = someMethod("hello");
    Assert.assertEquals(result, "hello world");
  }
}
