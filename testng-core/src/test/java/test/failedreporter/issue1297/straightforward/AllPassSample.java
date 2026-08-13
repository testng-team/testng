package test.failedreporter.issue1297.straightforward;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AllPassSample {
  @BeforeClass
  public void beforeClassAllPassSample() {}

  @Test
  public void newTest1() {}

  @AfterClass
  public void afterClassAllPassSample() {}
}
