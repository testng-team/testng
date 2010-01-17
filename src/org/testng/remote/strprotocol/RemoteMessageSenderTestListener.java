package org.testng.remote.strprotocol;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;
import org.testng.xml.XmlTest;

/**
 * A special listener that remote the event with string protocol.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class RemoteMessageSenderTestListener implements IResultListener {
  private final StringMessageSenderHelper m_sender;
  private ISuite m_suite;
  private XmlTest m_xmlTest;
  private ITestContext m_currentTestContext;
  
  public RemoteMessageSenderTestListener(ISuite suite, XmlTest test, StringMessageSenderHelper msh) {
    m_sender = msh;
    m_suite= suite;
    m_xmlTest= test;
  }

  public void onStart(ITestContext testCtx) {
    m_currentTestContext = testCtx;
    m_sender.sendMessage(new TestMessage(testCtx, true /*start*/));
  }

  public void onFinish(ITestContext testCtx) {
    m_sender.sendMessage(new TestMessage(testCtx, false /*end*/));
    m_currentTestContext = null;
  }

  public void onTestStart(ITestResult testResult) {
    TestResultMessage trm= null;
    
    if (null == m_currentTestContext) {
      trm= new TestResultMessage(m_suite.getName(), m_xmlTest.getName(), testResult);
    }
    else {
      trm= new TestResultMessage(m_currentTestContext, testResult);
    }
    
    m_sender.sendMessage(trm);
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult testResult) {
    if (null == m_currentTestContext) {
      m_sender.sendMessage(new TestResultMessage(m_suite.getName(), m_xmlTest.getName(), testResult));
    }
    else {
      m_sender.sendMessage(new TestResultMessage(m_currentTestContext, testResult));
    }
  }

  public void onTestFailure(ITestResult testResult) {
    if (null == m_currentTestContext) {
      m_sender.sendMessage(new TestResultMessage(m_suite.getName(), m_xmlTest.getName(), testResult));
    }
    else {
      m_sender.sendMessage(new TestResultMessage(m_currentTestContext, testResult));
    }
  }

  public void onTestSkipped(ITestResult testResult) {
    if (null == m_currentTestContext) {
      m_sender.sendMessage(new TestResultMessage(m_suite.getName(), m_xmlTest.getName(), testResult));
    }
    else {
      m_sender.sendMessage(new TestResultMessage(m_currentTestContext, testResult));
    }
  }

  public void onTestSuccess(ITestResult testResult) {
    if (null == m_currentTestContext) {
      m_sender.sendMessage(new TestResultMessage(m_suite.getName(), m_xmlTest.getName(), testResult));
    }
    else {
      m_sender.sendMessage(new TestResultMessage(m_currentTestContext, testResult));
    }
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
   */
  public void onConfigurationFailure(ITestResult itr) {
    onTestFailure(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
   */
  public void onConfigurationSkip(ITestResult itr) {
    onTestSkipped(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
   */
  public void onConfigurationSuccess(ITestResult itr) {
  }
}