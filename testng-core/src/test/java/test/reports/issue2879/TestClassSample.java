package test.reports.issue2879;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void passingOne() {}

  @Test
  public void passingTwo() {}

  @Test
  public void failingOne() {
    fail();
  }

  @Test
  public void failingTwo() {
    fail();
  }
}
