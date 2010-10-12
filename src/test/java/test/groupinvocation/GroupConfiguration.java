package test.groupinvocation;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;


/**
 * This class/interface
 */
public class GroupConfiguration {
  @BeforeGroups(groups={"a"})
  public void beforeGroups() {
    DummyTest.recordInvocation("beforeGroups", hashCode());
  }

  @AfterGroups(groups={"a"})
  public void afterGroups() {
    DummyTest.recordInvocation("afterGroups", hashCode());
  }
}
