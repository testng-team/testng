package test.configuration;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class SuiteFactoryOnceSample1Test {
  public static int m_before;
  public static int m_after;

  @BeforeSuite
  public void bs() {
    m_before++;
  }

  @AfterSuite
  public void as() {
    m_after++;
  }

  @Test
  public void g1() {}
}
