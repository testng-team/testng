package test.failedreporter.issue1297.straightforward;

import org.testng.annotations.*;

public class AllPassSample {
  @BeforeClass
  public void beforeClassAllPassSample() {}

  @Test
  public void newTest1() {}

  @AfterClass
  public void afterClassAllPassSample() {}
}
