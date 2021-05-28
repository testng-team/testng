package test.groupbug;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

public class ITCaseOne {
  private static final Logger log = Logger.getLogger(ITCaseOne.class);

  @BeforeClass
  public void beforeClassOne() {
    log.debug("RUN " + getClass() + ".beforeClass()");
  }

  @AfterClass(alwaysRun = true)
  public void afterClassOne() {
    log.debug("RUN " + getClass() + ".afterClass()");
  }

  @Test(groups = "std-one")
  public void one1() {
    log.debug("RUN " + getClass() + ".one1()");
  }

  /** Commenting out dependsOnGroups fixes the ordering, that's the bug. */
  @Test(groups = "logic-one", dependsOnGroups = "std-one")
  public void one2() {
    log.debug("RUN " + getClass() + ".one2()");
  }
}
