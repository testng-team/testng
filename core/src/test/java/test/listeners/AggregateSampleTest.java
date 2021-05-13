package test.listeners;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(L2.class)
public class AggregateSampleTest extends BaseAggregate {
  static int m_count = 0;
  static public void incrementCount() {
    m_count++;
  }

  @Test
  public void f() {
  }
}
