package org.testng.xml;

import static org.testng.reporters.XMLReporterConfig.ATTR_DESC;
import static org.testng.reporters.XMLReporterConfig.ATTR_DURATION_MS;
import static org.testng.reporters.XMLReporterConfig.ATTR_NAME;
import static org.testng.reporters.XMLReporterConfig.ATTR_STATUS;
import static org.testng.reporters.XMLReporterConfig.TAG_CLASS;
import static org.testng.reporters.XMLReporterConfig.TAG_PARAMS;
import static org.testng.reporters.XMLReporterConfig.TAG_SUITE;
import static org.testng.reporters.XMLReporterConfig.TAG_TEST;
import static org.testng.reporters.XMLReporterConfig.TAG_TEST_METHOD;

import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.remote.strprotocol.GenericMessage;
import org.testng.remote.strprotocol.IRemoteSuiteListener;
import org.testng.remote.strprotocol.IRemoteTestListener;
import org.testng.remote.strprotocol.MessageHelper;
import org.testng.remote.strprotocol.SuiteMessage;
import org.testng.remote.strprotocol.TestMessage;
import org.testng.remote.strprotocol.TestResultMessage;
import org.testng.reporters.XMLReporterConfig;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

/**
 * Parses testng-result.xml, create TestResultMessages and send them back to the listener
 * as we encounter them.
 *
 * @author Cedric Beust <cedric@beust.com>
 */
public class ResultContentHandler extends DefaultHandler {
  private int m_suiteMethodCount = 0;
  private int m_testMethodCount = 0;
  private SuiteMessage m_currentSuite;
  private TestMessage m_currentTest;
  private String m_className;
  private int m_passed;
  private int m_failed;
  private int m_skipped;
  private int m_invocationCount;
  private int m_currentInvocationCount;
  private TestResultMessage m_currentTestResult;
  private IRemoteSuiteListener m_suiteListener;
  private IRemoteTestListener m_testListener;
  private List<String> m_params = null;

  public ResultContentHandler(IRemoteSuiteListener suiteListener,
      IRemoteTestListener testListener, boolean resolveClasses /* ignored */) {
    m_suiteListener = suiteListener;
    m_testListener = testListener;
  }

  @Override
  public void startElement (String uri, String localName,
      String qName, Attributes attributes) {
    p("Start " + qName);
    if (TAG_SUITE.equals(qName)) {
      m_suiteListener.onInitialization(new GenericMessage(MessageHelper.GENERIC_SUITE_COUNT));
      m_suiteMethodCount = 0;
      m_currentSuite = new SuiteMessage(attributes.getValue(ATTR_NAME),
          true /* start */, m_suiteMethodCount);
      m_suiteListener.onStart(m_currentSuite);
    } else if (TAG_TEST.equals(qName)) {
      m_passed = m_failed = m_skipped = 0;
      m_currentTest = new TestMessage(true /* start */, m_currentSuite.getSuiteName(),
          attributes.getValue(ATTR_NAME), m_testMethodCount,
          m_passed, m_failed, m_skipped, 0);
      m_testListener.onStart(m_currentTest);
    } else if (TAG_CLASS.equals(qName)) {
      m_className = attributes.getValue(ATTR_NAME);
    } else if (TAG_TEST_METHOD.equals(qName)) {
      Integer status = XMLReporterConfig.getStatus(attributes.getValue(ATTR_STATUS));
      m_currentTestResult = new TestResultMessage(status, m_currentSuite.getSuiteName(),
          m_currentTest.getTestName(), m_className, attributes.getValue(ATTR_NAME),
          attributes.getValue(ATTR_DESC),
          attributes.getValue(ATTR_DESC),
          new String[0], /* no parameters, filled later */
          0, Long.parseLong(attributes.getValue(ATTR_DURATION_MS)),
          "" /* stack trace, filled later */,
          m_invocationCount, m_currentInvocationCount);
      m_suiteMethodCount++;
      m_testMethodCount++;
      if (status == ITestResult.SUCCESS) m_passed++;
      else if (status == ITestResult.FAILURE) m_failed++;
      else if (status == ITestResult.SKIP) m_skipped++;
    } else if (TAG_PARAMS.equals(qName)) {
      m_params = Lists.newArrayList();
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (m_params != null) {
      String string = new String(ch, start, length);
      String parameter = string;
      if (parameter.trim().length() != 0) {
        m_params.add(parameter);
      }
    }
  }

  @Override
  public void endElement (String uri, String localName, String qName) {
    if (TAG_SUITE.equals(qName)) {
      m_suiteListener.onFinish(new SuiteMessage(null, false /* end */, m_suiteMethodCount));
      m_currentSuite = null;
    } else if (TAG_TEST.equals(qName)) {
      m_currentTest = new TestMessage(false /* start */, m_currentSuite.getSuiteName(),
          null, m_testMethodCount,
          m_passed, m_failed, m_skipped, 0);
      m_testMethodCount = 0;
      m_testListener.onFinish(m_currentTest);
    } else if (TAG_CLASS.equals(qName)) {
      m_className = null;
    } else if (TAG_TEST_METHOD.equals(qName)) {
      switch(m_currentTestResult.getResult()) {
      case ITestResult.SUCCESS:
        m_testListener.onTestSuccess(m_currentTestResult);
        break;
      case ITestResult.FAILURE:
        m_testListener.onTestFailure(m_currentTestResult);
        break;
      case ITestResult.SKIP:
        m_testListener.onTestSkipped(m_currentTestResult);
        break;
      default:
       p("Ignoring test status:" + m_currentTestResult.getResult());
      }
    }
    else if (TAG_PARAMS.equals(qName)) {
      String[] params = new String[m_params.size()];
      for (int i = 0; i < m_params.size(); i++) {
        // The parameters are encoded  as type:value. Since we only care about the
        // value (and we don't receive the type anyway), use a dummy character in
        // its place
        params[i] = "@:" + m_params.get(i);
      }
      m_currentTestResult.setParameters(params);
      m_params = null;
    }
  }

  private static void p(String string) {
    if (false) {
      System.out.println("[ResultContentHandler] " + string);
    }
  }
}

