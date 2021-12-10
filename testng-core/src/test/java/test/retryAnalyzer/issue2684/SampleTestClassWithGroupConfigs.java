package test.retryAnalyzer.issue2684;

import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class SampleTestClassWithGroupConfigs {

  public static final List<String> logs = new ArrayList<>();

  @BeforeGroups("p1")
  public void beforeGroups() {
    logs.add("beforeGroups");
  }

  @Test(groups = "p1", retryAnalyzer = RerunAnalyzer.class)
  public void testMethod() {
    logs.add("testMethod");
    RerunAnalyzer.secondTestRetryCount++;
    assertTrue(RerunAnalyzer.secondTestRetryCount > RerunAnalyzer.maxRetryCount);
  }

  @AfterGroups("p1")
  public void afterGroups() {
    logs.add("afterGroups");
  }
}
