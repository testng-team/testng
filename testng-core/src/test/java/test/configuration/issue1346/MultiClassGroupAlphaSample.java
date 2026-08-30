package test.configuration.issue1346;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * Carries the group configuration for a group spanning it and {@link MultiClassGroupBravoSample}.
 */
public class MultiClassGroupAlphaSample {

  @BeforeGroups("mc")
  public void beforeGroups() {}

  @BeforeClass
  public void alphaBeforeClass() {}

  @Test(groups = "mc")
  public void alphaTest() {}

  @AfterClass
  public void alphaAfterClass() {}

  @AfterGroups("mc")
  public void afterGroups() {}
}
