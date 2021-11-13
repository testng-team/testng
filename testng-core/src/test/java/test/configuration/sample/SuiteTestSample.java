package test.configuration.sample;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class SuiteTestSample extends BaseSuiteTestSample {
  @BeforeSuite(dependsOnMethods = {"beforeSuiteParent"})
  public void beforeSuiteChild() {
    m_order.add(2);
  }

  @AfterSuite(dependsOnMethods = {"afterSuiteParent"})
  public void afterSuiteChild() {
    m_order.add(5);
  }

  @Test
  public void test1() {
    m_order.add(3);
  }
}
