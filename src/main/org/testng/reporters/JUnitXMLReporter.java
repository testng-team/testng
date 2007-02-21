package org.testng.reporters;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener;
import org.testng.internal.Utils;

/**
 * A JUnit XML report generator (replacing the original JUnitXMLReporter that was
 * based on XML APIs).
 *
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class JUnitXMLReporter implements IResultListener {

  private String m_outputFileName= null;
  private File m_outputFile= null;
  private ITestContext m_testContext= null;

  /**
   * keep lists of all the results
   */
  private int m_numPassed= 0;
  private int m_numFailed= 0;
  private int m_numSkipped= 0;
  private int m_numFailedButIgnored= 0;
  private List<ITestResult> m_allTests= Collections.synchronizedList(new ArrayList<ITestResult>());
  private List<ITestResult> m_configIssues= Collections.synchronizedList(new ArrayList<ITestResult>());

  public void onTestStart(ITestResult result) {
  }

  /**
   * Invoked each time a test succeeds.
   */
  public void onTestSuccess(ITestResult tr) {
    m_allTests.add(tr);
    m_numPassed++;
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
    m_allTests.add(tr);
    m_numFailedButIgnored++;
  }

  /**
   * Invoked each time a test fails.
   */
  public void onTestFailure(ITestResult tr) {
    m_allTests.add(tr);
    m_numFailed++;
  }

  /**
   * Invoked each time a test is skipped.
   */
  public void onTestSkipped(ITestResult tr) {
    m_allTests.add(tr);
    m_numSkipped++;
  }

  /**
   * Invoked after the test class is instantiated and before
   * any configuration method is called.
   *
   */
  public void onStart(ITestContext context) {
    m_outputFileName= context.getOutputDirectory() + File.separator + context.getName() + ".xml";
    m_outputFile= new File(m_outputFileName);
    m_testContext= context;
  }

  /**
   * Invoked after all the tests have run and all their
   * Configuration methods have been called.
   *
   */
  public void onFinish(ITestContext context) {
    generateReport();
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationFailure(org.testng.ITestResult)
   */
  public void onConfigurationFailure(ITestResult itr) {
    m_configIssues.add(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSkip(org.testng.ITestResult)
   */
  public void onConfigurationSkip(ITestResult itr) {
    m_configIssues.add(itr);
  }

  /**
   * @see org.testng.internal.IConfigurationListener#onConfigurationSuccess(org.testng.ITestResult)
   */
  public void onConfigurationSuccess(ITestResult itr) {
  }

  /**
   * generate the XML report given what we know from all the test results
   */
  protected void generateReport() {
    try {
      XMLStringBuffer document= new XMLStringBuffer("");
      document.setXmlDetails("1.0", "UTF-8");
      Properties attrs= new Properties();
      attrs.setProperty(XMLConstants.ATTR_NAME, m_testContext.getName());
      attrs.setProperty(XMLConstants.ATTR_TESTS, "" + m_allTests.size());
      attrs.setProperty(XMLConstants.ATTR_FAILURES, "" + m_numFailed);
      attrs.setProperty(XMLConstants.ATTR_ERRORS, "0");
      attrs.setProperty(XMLConstants.ATTR_TIME, ""
          + ((m_testContext.getEndDate().getTime() - m_testContext.getStartDate().getTime()) / 1000.0));

      document.push(XMLConstants.TESTSUITE, attrs);
      document.addEmptyElement(XMLConstants.PROPERTIES);

      for(ITestResult tr : m_configIssues) {
        createElement(document, tr);
      }
      for(ITestResult tr : m_allTests) {
        createElement(document, tr);
      }

      document.pop();
      BufferedWriter fw= new BufferedWriter(new FileWriter(m_outputFile));
      fw.write(document.toXML());
      fw.flush();
      fw.close();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
      System.err.println("failed to create JUnitXML because of " + ioe);
    }
  }

  private void createElement(XMLStringBuffer doc, ITestResult tr) {
    Properties attrs= new Properties();
    long elapsedTimeMillis= tr.getEndMillis() - tr.getStartMillis();
    String name= tr.getMethod().isTest() ? tr.getName() : Utils.detailedMethodName(tr.getMethod(), false);
    attrs.setProperty(XMLConstants.ATTR_NAME, name);
    attrs.setProperty(XMLConstants.ATTR_CLASSNAME, tr.getTestClass().getRealClass().getName());
    attrs.setProperty(XMLConstants.ATTR_TIME, "" + (((double) elapsedTimeMillis) / 1000));

    if((ITestResult.FAILURE == tr.getStatus()) || (ITestResult.SKIP == tr.getStatus())) {
      doc.push(XMLConstants.TESTCASE, attrs);

      if(ITestResult.FAILURE == tr.getStatus()) {
        createFailureElement(doc, tr);
      }
      else if(ITestResult.SKIP == tr.getStatus()) {
        createSkipElement(doc, tr);
      }

      doc.pop();
    }
    else {
      doc.addEmptyElement(XMLConstants.TESTCASE, attrs);
    }
  }

  private void createFailureElement(XMLStringBuffer doc, ITestResult tr) {
    Properties attrs= new Properties();
    Throwable t= tr.getThrowable();
    if(t != null) {
      attrs.setProperty(XMLConstants.ATTR_TYPE, t.getClass().getName());
      String message= t.getMessage();
      if((message != null) && (message.length() > 0)) {
        attrs.setProperty(XMLConstants.ATTR_MESSAGE, message);
      }
      doc.push(XMLConstants.FAILURE, attrs);
      doc.addCDATA(Utils.stackTrace(t, false)[0]);
      doc.pop();
    }
    else {
      doc.addEmptyElement(XMLConstants.FAILURE); // THIS IS AN ERROR
    }
  }

  private void createSkipElement(XMLStringBuffer doc, ITestResult tr) {
    doc.addEmptyElement("skipped");
  }
}
