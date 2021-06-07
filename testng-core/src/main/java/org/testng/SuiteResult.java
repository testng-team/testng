package org.testng;

import javax.annotation.Nonnull;
import org.testng.collections.Objects;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;

/** This class logs the result of an entire Test Suite (defined by a property file). */
class SuiteResult implements ISuiteResult, Comparable<ISuiteResult> {
  private final XmlSuite m_suite;
  private final ITestContext m_testContext;

  protected SuiteResult(XmlSuite suite, ITestContext tr) {
    m_suite = suite;
    m_testContext = tr;
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
  public int compareTo(@Nonnull ISuiteResult other) {
    int result = 0;
    try {
      String n1 = getTestContext().getName();
      String n2 = other.getTestContext().getName();
      result = n1.compareTo(n2);
    } catch (Exception ex) {
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
