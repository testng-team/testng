package test.beforegroups.issue2229;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class AnotherTestClassSample {

  public static List<String> logs = new ArrayList<>();

  @BeforeGroups(groups = "testGroup1")
  public void beforeGroups1() {
    logs.add("beforeGroups1");
  }

  @BeforeGroups(groups = "testGroup2")
  public void beforeGroups2() {
    logs.add("beforeGroups2");
  }

  @AfterGroups(groups = "testGroup1")
  public void afterGroups1() {
    logs.add("afterGroups1");
  }

  @AfterGroups(groups = "testGroup2")
  public void afterGroups2() {
    logs.add("afterGroups2");
  }

  @Test(groups = "testGroup1")
  public void test1() {
    logs.add("test1_testGroup1");
  }

  @Test(groups = "testGroup2")
  public void test2() {
    logs.add("test2_testGroup2");
  }

  @Test(groups = "testGroup1")
  public void test3() {
    logs.add("test3_testGroup1");
  }

  @Test(groups = "testGroup2")
  public void test4() {
    logs.add("test4_testGroup2");
  }
}
