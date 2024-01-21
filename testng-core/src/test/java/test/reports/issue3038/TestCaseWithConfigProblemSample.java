package test.reports.issue3038;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestCaseWithConfigProblemSample {

  @BeforeMethod
  public void beforeMethod() {
    throw new RuntimeException("simulating a failure");
  }

  @Test
  public void testHello() {}
}
