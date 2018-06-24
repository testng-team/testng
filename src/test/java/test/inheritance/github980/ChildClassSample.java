package test.inheritance.github980;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ChildClassSample extends ParentClassSample {
  @Test
  public void c() {
    Assert.assertTrue(true);
  }

  @Test
  public void d() {
    Assert.assertTrue(true);
  }
}
