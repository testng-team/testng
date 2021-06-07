package test.reports.issue2445;

import org.junit.Assert;
import org.testng.annotations.Test;

public class Test2 {
  @Test
  public void test2() {
    Assert.assertEquals("Simulate success", "1", "1");
  }
}
