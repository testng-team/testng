package org.testng.reporters;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.Lists;
import org.testng.internal.IResultListener;
import org.testng.internal.Utils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * this XML Reporter will produce XML format compatible with the XMLJUnitResultFormatter from ant
 * this enables TestNG output to be processed by tools that already handle this format
 *
 * borrows heavily from ideas in org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter
 * 
 * this is the original version which was replaced by JUnitXMLReporter
 * @TODO: clean up
 */
public class JUnitXMLReporter2 implements IResultListener {

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
  private List<ITestResult> m_allTests 
      = Collections.synchronizedList(Lists.<ITestResult>newArrayList());
  private List<ITestResult> m_configIssues
      = Collections.synchronizedList(Lists.<ITestResult>newArrayList());

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
   * generate the XML report given what we know from all the test results
   */
  protected void generateReport() {
    try {
      DocumentBuilderFactory docBuilderFactory= DocumentBuilderFactory.newInstance();
      docBuilderFactory.setNamespaceAware(true); // so we can transform it later
      DocumentBuilder docBuilder= docBuilderFactory.newDocumentBuilder();
      Document d= docBuilder.newDocument();
      Element rootElement= d.createElement(XMLConstants.TESTSUITE);
      rootElement.setAttribute(XMLConstants.ATTR_NAME, m_testContext.getName());

      Element propsElement= d.createElement(XMLConstants.PROPERTIES);
      rootElement.appendChild(propsElement);

      // properties. just TestNG properties or also System properties?
      ISuite suite= m_testContext.getSuite();

      rootElement.setAttribute(XMLConstants.ATTR_TESTS, "" + m_allTests.size());
      rootElement.setAttribute(XMLConstants.ATTR_FAILURES, "" + m_numFailed);
      rootElement.setAttribute(XMLConstants.ATTR_ERRORS, "0"); // FIXME

      long elapsedTimeMillis= m_testContext.getEndDate().getTime()
        - m_testContext.getStartDate().getTime();
      rootElement.setAttribute(XMLConstants.ATTR_TIME, "" + (elapsedTimeMillis / 1000.0));

      for(ITestResult tr: m_configIssues) {
        Element element= createElement(d, tr);
        rootElement.appendChild(element);
      }
      for(ITestResult tr : m_allTests) {
        Element testCaseElement= createElement(d, tr);
        rootElement.appendChild(testCaseElement);
      }

      BufferedWriter fw= new BufferedWriter(new FileWriter(m_outputFile));

      Transformer transformer= TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(rootElement), new StreamResult(fw));
      fw.flush();
      fw.close();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
      System.err.println("failed to create JUnitXML because of " + ioe);
    }
    catch(ParserConfigurationException pce) {
      pce.printStackTrace();
      System.err.println("failed to create JUnitXML because of " + pce);
    }
    catch(TransformerException te) {
      te.printStackTrace();
      System.err.println("Error while writing out JUnitXML because of " + te);
    }
  }

  private Element createElement(Document doc, ITestResult tr) {
    Element resultElement= doc.createElement(XMLConstants.TESTCASE);
    long elapsedTimeMillis= tr.getEndMillis() - tr.getStartMillis();
    String name= tr.getMethod().isTest() ? tr.getName() : Utils.detailedMethodName(tr.getMethod(), false);
    resultElement.setAttribute(XMLConstants.ATTR_NAME, name);
    resultElement.setAttribute(XMLConstants.ATTR_CLASSNAME,
                               tr.getTestClass().getRealClass().getName());
    resultElement.setAttribute(XMLConstants.ATTR_TIME, 
                               "" + ((double) elapsedTimeMillis)/1000);
    if (ITestResult.FAILURE == tr.getStatus()) {
      Element nested = createFailureElement(doc, tr); 
      resultElement.appendChild(nested);
    }
    else if (ITestResult.SKIP == tr.getStatus()) {
      Element nested = createSkipElement(doc, tr);
      resultElement.appendChild(nested);
    }
    
    return resultElement;
  }
  
  private Element createFailureElement(Document doc, ITestResult tr) {
    Element nested= doc.createElement(XMLConstants.FAILURE);
    Throwable t = tr.getThrowable();
    if (t != null) {
      nested.setAttribute(XMLConstants.ATTR_TYPE, t.getClass().getName());
      String message = t.getMessage();
      if ((message != null) && (message.length() > 0)) {
        nested.setAttribute(XMLConstants.ATTR_MESSAGE, message);
      }
      CDATASection trace= doc.createCDATASection(Utils.stackTrace(t, false)[0]);
      nested.appendChild(trace);
    }
    
    return nested;
  }
  
  private Element createSkipElement(Document doc, ITestResult tr) {
    return doc.createElement("skipped");
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
}
