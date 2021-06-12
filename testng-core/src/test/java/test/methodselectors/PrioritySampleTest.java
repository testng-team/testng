package test.methodselectors;

import org.testng.annotations.Test;

public class PrioritySampleTest {

  @Test
  public void alwaysRun() {}

  @Test
  @NoTest
  public void neverRun() {}
}
