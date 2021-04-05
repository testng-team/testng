package test.configuration;

import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.List;

public class BaseSuiteTest {
  public static List<Integer> m_order;

  @BeforeSuite
  public void beforeSuiteParent() {
    m_order = new ArrayList<>();
    m_order.add(1);
  }
}
