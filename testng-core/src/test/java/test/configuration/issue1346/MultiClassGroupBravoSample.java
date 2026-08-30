package test.configuration.issue1346;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** The second class of the {@code mc} group; it declares no group configuration of its own. */
public class MultiClassGroupBravoSample {

  @BeforeClass
  public void bravoBeforeClass() {}

  @Test(groups = "mc")
  public void bravoTest() {}

  @AfterClass
  public void bravoAfterClass() {}
}
