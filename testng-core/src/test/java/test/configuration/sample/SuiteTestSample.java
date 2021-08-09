package test.configuration.sample;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import test.configuration.BaseSuiteTest;

public class SuiteTestSample extends BaseSuiteTest {
  @BeforeSuite(dependsOnMethods = {"beforeSuiteParent"})
  public void beforeSuiteChild() {
    m_order.add(2);
  }

  @Test
  public void test1() {
    m_order.add(3);
  }
}
