package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class Shadow2SampleTest {
  @Parameters("a")
  @Test
  public void test2(String a) {
    Assert.assertEquals("Second", a);
  }

}
