package org.testng;

import org.testng.collections.Objects;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;

import javax.annotation.Nonnull;

/** This class logs the result of an entire Test Suite (defined by a property file). */
class SuiteResult implements ISuiteResult, Comparable {
  // FIXME: Is m_propertyFileName needed?
  private String m_propertyFileName = null;
  private final XmlSuite m_suite;
  private final ITestContext m_testContext;

  protected SuiteResult(XmlSuite suite, ITestContext tr) {
    m_suite = suite;
    m_testContext = tr;
  }

  /** @return Returns the propertyFileName. */
  @Override
  public String getPropertyFileName() {
    return m_propertyFileName;
  }

  /** @return Returns the singleTestRunner. */
  @Override
  public ITestContext getTestContext() {
    return m_testContext;
  }
  /** @return Returns the suite. */
  public XmlSuite getSuite() {
    return m_suite;
  }

  @Override
  public int compareTo(@Nonnull Object o) {
    int result = 0;
    try {
      SuiteResult other = (SuiteResult) o;
      String n1 = getTestContext().getName();
      String n2 = other.getTestContext().getName();
      result = n1.compareTo(n2);
    } catch (Exception ex) {
      ex.printStackTrace();
      Logger.getLogger(SuiteResult.class).error(ex.getMessage(), ex);
    }

    return result;
  }

  /** Returns the test context name. {@inheritDoc} */
  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("context", getTestContext().getName()).toString();
  }
}
