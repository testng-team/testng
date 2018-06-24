package test.groupinvocation;

import org.testng.annotations.BeforeGroups;

public class FailingBeforeGroupMethod {
  @BeforeGroups(groups = {"A"})
  public void beforeGroupA() {
    throw new RuntimeException("Failing @BeforeGroups beforeGroupA method");
  }
}
