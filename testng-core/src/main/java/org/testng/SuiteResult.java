package org.testng;

import org.testng.collections.Objects;
import org.testng.xml.XmlSuite;

/**
 * Holds the result of one {@code <test>} in a suite.
 *
 * <p>A suite with three {@code <test>} tags gets three of these objects. {@link SuiteRunner}
 * creates each one when its {@code <test>} ends. {@link ISuite#getResults()} returns them, keyed by
 * the name of each {@code <test>}.
 */
class SuiteResult implements ISuiteResult, Comparable<SuiteResult> {

  private final XmlSuite m_suite;
  private final ITestContext m_testContext;

  /**
   * Creates the result of one {@code <test>}.
   *
   * @param suite the suite that holds the {@code <test>}.
   * @param tr the context of the {@code <test>}, which holds its results.
   */
  protected SuiteResult(XmlSuite suite, ITestContext tr) {
    m_suite = suite;
    m_testContext = tr;
  }

  /** Returns the context of the {@code <test>}, which holds its results. */
  @Override
  public ITestContext getTestContext() {
    return m_testContext;
  }
  /**
   * Returns the suite that holds the {@code <test>}.
   *
   * @return the suite.
   */
  public XmlSuite getSuite() {
    return m_suite;
  }

  @Override
  public int compareTo(SuiteResult other) {
    return getTestContext().getName().compareTo(other.getTestContext().getName());
  }

  /** Returns a short text that names this class and the {@code <test>}, for debugging. */
  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("context", getTestContext().getName()).toString();
  }
}
