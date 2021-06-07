package test.junitreports;

import org.testng.annotations.Test;

public class Issue1262TestSample {

  @Test(priority = 3)
  public void testRoles003_Post() {}

  @Test(priority = 4)
  public void testRoles004_Post() {}

  @Test(priority = 1)
  public void testRoles001_Post() {}

  @Test(priority = 2)
  public void testRoles002_Post() {}
}
