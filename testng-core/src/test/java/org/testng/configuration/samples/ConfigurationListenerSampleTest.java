package org.testng.configuration.samples;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.configuration.samples.ConfigurationListenerSampleTest.MyTLA;

@Listeners(MyTLA.class)
public class ConfigurationListenerSampleTest {
  public static boolean m_passed = false;

  public static class MyTLA extends TestListenerAdapter {
    @Override
    public void onConfigurationFailure(ITestResult itr) {
      m_passed = true;
    }
  }

  @BeforeClass
  public void bm() {
    m_passed = false;
    throw new RuntimeException();
  }

  @Test
  public void f1() {}
}
