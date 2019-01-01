package test.beforegroups.issue118;

import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Test(groups = "group1")
public class TestclassSample {
  private Object testObject;

  @BeforeGroups(inheritGroups = true)
  public void setUpGroup() {
    testObject = new Object();
  }

  public void test1() {
    Assert.assertNotNull(testObject, "@BeforeGroups not invoked if nothing explicitly specified");
  }
}
