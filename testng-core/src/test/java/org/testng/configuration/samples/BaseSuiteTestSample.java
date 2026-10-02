package org.testng.configuration.samples;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseSuiteTestSample {
  public static List<Integer> m_order;

  @BeforeSuite
  public void beforeSuiteParent() {
    m_order = new ArrayList<>();
    m_order.add(1);
  }

  @AfterSuite
  public void afterSuiteParent() {
    m_order.add(4);
  }
}
