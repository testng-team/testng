package test.configuration.issue2670;

import org.testng.annotations.BeforeClass;

public abstract class Issue2670BaseSample {

  @BeforeClass(alwaysRun = true)
  protected void beforeClassParent() {
    System.out.println("TestParent.beforeClassParent");
  }
}
