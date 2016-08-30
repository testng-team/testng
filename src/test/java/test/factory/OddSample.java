package test.factory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OddSample {

  private final int n;

  public OddSample(int n) {
    this.n = n;
  }

  @Test
  public void verify() {
    Assert.assertNotEquals(n % 2, 0);
  }
}
