package test.configurationfailurepolicy;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class ClassWithFailedBeforeClassMethodAndBeforeGroupsAfterClassAfterGroups {

  @BeforeClass
  public void setupClassFails() {
    throw new RuntimeException("setup class fail");
  }

  @BeforeGroups(groups = "group1")
  public void beforeGroup() {}

  @Test(groups = "group1")
  public void test1() {}

  @AfterClass
  public void tearDownClass() {}

  @AfterGroups(groups = "group1")
  public void afterGroup() {}
}
