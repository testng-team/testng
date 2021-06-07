package test.beforegroups.issue346;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class SampleTestClass {

  public static Map<String, List<String>> logs = new HashMap<>();

  @BeforeGroups(groups = "A")
  @AfterGroups(groups = "B")
  public void setEnvironment() {
    ITestResult itr = Reporter.getCurrentTestResult();
    String type = itr.getMethod().isBeforeGroupsConfiguration() ? "beforeGroups" : "afterGroups";
    String[] groups = itr.getTestContext().getIncludedGroups();
    String testName = itr.getTestContext().getName();
    List<String> data =
        Arrays.stream(groups).map(g -> type + ":" + testName + g).collect(Collectors.toList());
    logs.put(itr.getTestContext().getName(), data);
  }

  @Test(groups = "A")
  public void test1() {}

  @Test(groups = "B")
  public void test2() {}
}
