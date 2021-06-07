package test.parameters.issue2238;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ExampleTestCase {

  @Test
  @Parameters("value")
  public void testMethod(int value) {
    int expected = Integer.parseInt(System.getProperty("value"));
    Assert.assertEquals(value, expected);
  }
}
