package test.reports.issue2445;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class Test1 {
  @Test
  public void test1() {
    fail("Simulate failure");
  }
}
