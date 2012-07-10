package test.parameters;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class MultiIncludeSampleTest {
  
  private int callCount = 0;
  
  @Parameters("num")
  @Test
  public void multiIncludeTest(int num) {
    callCount++;
    Assert.assertEquals(callCount, num);
  }
}
