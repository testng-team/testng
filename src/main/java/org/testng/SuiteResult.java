package org.testng;

import java.util.Comparator;

import org.testng.xml.XmlSuite;


/**
 * This class logs the result of an entire Test Suite (defined by a 
 * property file).
 *
 * @author Cedric Beust, May 10, 2004
 * 
 */
public class SuiteResult implements ISuiteResult, Comparable {
	/* generated */
	private static final long serialVersionUID = 6778513869858860756L;
private String m_propertyFileName =  null;
  private XmlSuite m_suite = null;  
  private ITestContext m_testContext = null;
  
  public static final Comparator COMPARATOR = new Comparator<SuiteResult>() {
    public int compare(SuiteResult o1, SuiteResult o2) {
      return o1.getPropertyFileName().compareTo(o2.getPropertyFileName());
    }

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }
  };

  /**
   * @param propertyFileName
   * @param tr
   */
  public SuiteResult(String propertyFileName, ITestContext tr) {
    m_propertyFileName = propertyFileName;
    m_testContext = tr;
  }
  
  public SuiteResult(XmlSuite suite, ITestContext tr) {
    m_suite = suite;
    m_testContext = tr;
  }

  /**
   * @return Returns the propertyFileName.
   */
  public String getPropertyFileName() {
    return m_propertyFileName;
  }
  
  /**
   * @return Returns the singleTestRunner.
   */
  public ITestContext getTestContext() {
    return m_testContext;
  }
  /**
   * @return Returns the suite.
   */
  public XmlSuite getSuite() {
    return m_suite;
  }

  public int compareTo(Object o) {
    int result = 0;
    try {
      SuiteResult other = (SuiteResult) o;
      String n1 = getTestContext().getName();
      String n2 = other.getTestContext().getName();
      result = n1.compareTo(n2);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    
    return result;
  }
  
  /**
   * Returns the test context name. 
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "[SuiteResult " + getTestContext().getName() + "]";
  }
  
}
