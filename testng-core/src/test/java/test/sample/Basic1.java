package test.sample;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Basic1 {
  private static int m_count = 0;

  public static void incrementCount() {
    m_count++;
  }

  public static int getCount() {
    return m_count;
  }

  @BeforeMethod
  public void beforeTestMethod() {
    incrementCount();
  }

  @Test(groups = {"basic1"})
  public void basic1() {
    assert getCount() > 0 : "COUNT WAS NOT INCREMENTED";
  }
}
