package test.beforegroups.issue1694;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

public class BaseClassWithBeforeGroups {
  public static final String BEFORE_GROUPS = "beforeGroups";
  public static final String AFTER_GROUPS = "afterGroups";

  @BeforeGroups(value = "regression")
  public void beforeGroups() {}

  @AfterGroups(value = "regression")
  public void afterGroups() {}
}
