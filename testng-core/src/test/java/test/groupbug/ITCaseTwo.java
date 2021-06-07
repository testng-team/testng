package test.groupbug;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

public class ITCaseTwo {
  private static final Logger log = Logger.getLogger(ITCaseTwo.class);

  @BeforeClass
  public void beforeClassTwo() {
    log.debug("RUN " + getClass() + ".beforeClass()");
  }

  @AfterClass(alwaysRun = true)
  public void afterClassTwo() {
    log.debug("RUN " + getClass() + ".afterClass()");
  }

  @Test(groups = "std-two")
  public void two1() {
    log.debug("RUN " + getClass() + ".one1()");
  }

  @Test(groups = "logic-two", dependsOnGroups = "std-two")
  public void two2() {
    log.debug("RUN " + getClass() + ".one2()");
  }
}
