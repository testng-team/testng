package test.superclass;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ChildSampleTest3 extends BaseSampleTest3 {
  @Test
  public void pass() {
    Assert.assertTrue(true);
  }

  @Test
  public void fail() {
    Assert.assertTrue(false);
  }
}
