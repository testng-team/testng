package org.testng;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.testng.internal.config.MapConfigurationParser;

import java.util.Properties;

/**
 * Tests configuring / running a {@link TestNG} instance
 * via some of the utility methods provided - such as {@link TestNG#configureAndRun(java.util.Map)}.
 */
@Test
public class TestNGConfigureTest {

  public void test_Configure_And_Run() {

    TestNG test = new TestNG(false);
    ITestNGConfiguration parser = new MapConfigurationParser();

    ConfigTestListener listener = new ConfigTestListener();
    ConfigSuiteListener suiteListener = new ConfigSuiteListener();
    
    Properties props = new Properties();
    props.put(ITestNGConfiguration.TEST_CLASSES, "org.testng.MapConfigurationParserTest");
    props.put(ITestNGConfiguration.TEST_LISTENER, listener);
    props.put(ITestNGConfiguration.SUITE_LISTENER, suiteListener);
    
    test.configureAndRun(props);
    
    assert test.getStatus() != TestNG.HAS_FAILURE;
    assertEquals(listener.m_failure, 0);
    assertEquals(listener.m_success, 5);
    assertEquals(listener.m_skipped, 0);
    assertEquals(listener.m_started, 5);
    assertEquals(listener.m_contextStarted, 1);
    assertEquals(listener.m_contextFinished, 1);
    
    assertEquals(suiteListener.m_suiteStart, 1);
    assertEquals(suiteListener.m_suiteFinish, 1);
  }

  
  class ConfigSuiteListener implements ISuiteListener {

    int m_suiteStart, m_suiteFinish = 0;

    public void onStart(ISuite suite)
    {
      m_suiteStart++;
    }

    public void onFinish(ISuite suite)
    {
      m_suiteFinish++;
    }
  }

  class ConfigTestListener implements ITestListener {

    int m_success, m_failure, m_skipped, m_failedPercent, m_started, m_contextStarted, m_contextFinished = 0;

    public void onTestStart(ITestResult result)
    {
      m_started++;
    }

    public void onTestSuccess(ITestResult result)
    {
      m_success++;
    }

    public void onTestFailure(ITestResult result)
    {
      m_failure++;
    }

    public void onTestSkipped(ITestResult result)
    {
      m_skipped++;
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult result)
    {
      m_failedPercent++;
    }

    public void onStart(ITestContext context)
    {
      m_contextStarted++;
    }

    public void onFinish(ITestContext context)
    {
      m_contextFinished++;
    }
  }
}
