package test.order.github288;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public class BaseSample {

  @BeforeClass
  protected void beforeClass() {}

  @AfterClass(alwaysRun = true)
  protected void afterClass() {}
}
