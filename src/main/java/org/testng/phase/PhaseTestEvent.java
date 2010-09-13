package org.testng.phase;

import org.testng.xml.XmlTest;

public class PhaseTestEvent extends PhaseEvent {

  private XmlTest m_xmlTest;

  public PhaseTestEvent(String name, boolean before, XmlTest xmlTest) {
    super(name, before);
    m_xmlTest = xmlTest;
  }

  public XmlTest getXmlTest() {
    return m_xmlTest;
  }

  @Override
  public String toString() {
    return "[PhaseTestEvent " + getName() + (isBefore() ? " before " : " after ") + "]";
  }
}
