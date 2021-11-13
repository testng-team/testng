package test.annotationtransformer.issue2312;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleTestClass {

  @Test
  public void testMethod() {
    Assert.assertEquals(1, 1);
  }
}
