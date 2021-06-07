package test.configuration;

import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeSuite;

public class BaseSuiteTest {
  public static List<Integer> m_order;

  @BeforeSuite
  public void beforeSuiteParent() {
    m_order = new ArrayList<>();
    m_order.add(1);
  }
}
