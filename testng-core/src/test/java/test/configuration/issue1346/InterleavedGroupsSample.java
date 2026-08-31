package test.configuration.issue1346;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/**
 * One class whose test methods alternate between two groups, so the class enters {@code g2} well
 * after its own {@code @BeforeClass} has run. The class configuration is {@code alwaysRun} so that
 * the same sample also serves the group-filtering case, where a plain one would be filtered out.
 */
public class InterleavedGroupsSample {

  @BeforeClass(alwaysRun = true)
  public void beforeClass() {}

  @BeforeGroups("g1")
  public void beforeGroupsOne() {}

  @BeforeGroups("g2")
  public void beforeGroupsTwo() {}

  @Test(groups = "g1")
  public void test1() {}

  @Test(groups = "g2")
  public void test2() {}

  @Test(groups = "g1")
  public void test3() {}

  @Test(groups = "g2")
  public void test4() {}

  @AfterGroups("g1")
  public void afterGroupsOne() {}

  @AfterGroups("g2")
  public void afterGroupsTwo() {}

  @AfterClass(alwaysRun = true)
  public void afterClass() {}
}
