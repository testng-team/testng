package test.methodselectors;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(description = "GITHUB-1507")
public class ClassWithManyMethodsSample {

  public void testa() {
    Assert.assertTrue(true);
  }

  public void testb() {
    Assert.assertTrue(true);
  }

  public void testc() {
    Assert.assertTrue(true);
  }

  public void testd() {
    Assert.assertTrue(true);
  }
}
