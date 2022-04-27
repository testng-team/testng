package test.aftergroups.samples;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

public class MultipleGroupsSample {

  @AfterGroups(groups = {"group-1", "group-2", "not-defined"})
  public void afterGroup() {}

  @Test(groups = "group-1")
  public void test1() {}

  @Test(groups = "group-2")
  public void test2() throws InterruptedException {
    Thread.sleep(3000);
  }
}
