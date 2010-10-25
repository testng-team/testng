package test.dependent;

import org.testng.annotations.Test;

@Test(groups = {"integration"}, dependsOnGroups = {"checkin", "a"})
public class MultiGroup2SampleTest {

  public void test2() throws Exception {
  }
}
