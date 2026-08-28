package test.configuration.issue2663;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Used with parallel="classes" and with parallel="tests"; see {@link ParallelSampleOne}. */
public class ParallelSampleTwo {

  @BeforeClass(priority = 2)
  public void twoAlphaBeforeClass() {}

  @BeforeClass(priority = 1)
  public void twoBravoBeforeClass() {}

  @Test
  public void twoTest() {}
}
