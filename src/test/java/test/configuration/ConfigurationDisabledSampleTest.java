package test.configuration;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ConfigurationDisabledSampleTest {

  public static boolean m_afterWasRun;

  @BeforeClass(alwaysRun = true)
  public void before() {
    m_afterWasRun = false;
  }

  @AfterClass(alwaysRun = true)
  public void after() {
    m_afterWasRun = true;
  }

  @Test
  public void f1() {}

  @Test(enabled = false)
  public void f2() {}
}