package org.testng.phase;

import org.testng.xml.XmlSuite;

public class PhaseSuiteEvent extends PhaseEvent {

  private XmlSuite m_xmlSuite;

  public PhaseSuiteEvent(String name, boolean before, XmlSuite suite) {
    super(name, before);
    m_xmlSuite = suite;
  }

  public XmlSuite getXmlSuite() {
    return m_xmlSuite;
  }

  @Override
  public String toString() {
    return "[PhaseSuiteEvent " + getName() + (isBefore() ? " before " : " after ") + "]";
  }
}
