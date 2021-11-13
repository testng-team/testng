package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class Shadow1Sample {

  @Parameters("a")
  @Test
  public void test1(String a) {
    Assert.assertEquals(a, "First");
  }
}
