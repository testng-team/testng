package test.priority;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class SampleTestBase {
  @BeforeClass
  public void setUp() {}

  @AfterClass
  public void closeBrowser() {}
}
