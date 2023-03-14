package test.reports.issue2879;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void passingOne() {}

  @Test
  public void passingTwo() {}

  @Test
  public void failingOne() {
    Assert.fail();
  }

  @Test
  public void failingTwo() {
    Assert.fail();
  }
}
