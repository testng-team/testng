package test.configuration.issue1346;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

/** A test method belonging to two groups at once, so neither group encloses the other. */
public class TwoGroupsPerMethodSample {

  @BeforeClass
  public void beforeClass() {}

  @BeforeGroups("g1")
  public void beforeGroupsOne() {}

  @BeforeGroups("g2")
  public void beforeGroupsTwo() {}

  @Test(groups = {"g1", "g2"})
  public void test1() {}

  @Test(groups = "g1")
  public void test2() {}

  @AfterGroups("g1")
  public void afterGroupsOne() {}

  @AfterGroups("g2")
  public void afterGroupsTwo() {}

  @AfterClass
  public void afterClass() {}
}
