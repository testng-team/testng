package test.configuration.issue2663;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Used with parallel="classes" and with parallel="tests"; see {@link ParallelSampleTwo}. */
public class ParallelSampleOne {

  @BeforeClass(priority = 2)
  public void oneAlphaBeforeClass() {}

  @BeforeClass(priority = 1)
  public void oneBravoBeforeClass() {}

  @Test
  public void oneTest() {}
}
