package test.retryAnalyzer.issue2684;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SampleTestClassWithGroupConfigs {

  @BeforeSuite(alwaysRun = true)
  public void beforeSuite() {}

  @BeforeTest(alwaysRun = true)
  public void beforeTest() {}

  @BeforeGroups("2684_group")
  public void beforeGroups() {}

  @BeforeClass(alwaysRun = true)
  public void beforeClass() {}

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod() {}

  @Test(groups = "2684_group", retryAnalyzer = RerunAnalyzer.class)
  public void testMethod() {
    RerunAnalyzer.secondTestRetryCount++;
    assertTrue(RerunAnalyzer.secondTestRetryCount > RerunAnalyzer.maxRetryCount);
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod() {}

  @AfterClass(alwaysRun = true)
  public void afterClass() {}

  @AfterGroups("2684_group")
  public void afterGroups() {}

  @AfterTest(alwaysRun = true)
  public void afterTest() {}

  @AfterSuite(alwaysRun = true)
  public void afterSuite() {}
}
