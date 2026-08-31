package test.configuration.issue1346;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** Two instances of one class, all of whose test methods belong to the same group. */
public class FactoryGroupSample {

  @Factory
  public static Object[] instances() {
    return new Object[] {new FactoryGroupSample(), new FactoryGroupSample()};
  }

  @BeforeGroups("fg")
  public void beforeGroups() {}

  @BeforeClass
  public void beforeClass() {}

  @Test(groups = "fg")
  public void test() {}

  @AfterClass
  public void afterClass() {}

  @AfterGroups("fg")
  public void afterGroups() {}
}
