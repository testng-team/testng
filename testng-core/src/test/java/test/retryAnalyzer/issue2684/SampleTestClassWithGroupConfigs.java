package test.retryAnalyzer.issue2684;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

public class SampleTestClassWithGroupConfigs {

  @BeforeGroups("2684_group")
  public void beforeGroups() {}

  @Test(groups = "2684_group", retryAnalyzer = RerunAnalyzer.class)
  public void testMethod() {
    RerunAnalyzer.secondTestRetryCount++;
    assertTrue(RerunAnalyzer.secondTestRetryCount > RerunAnalyzer.maxRetryCount);
  }

  @AfterGroups("2684_group")
  public void afterGroups() {}
}
