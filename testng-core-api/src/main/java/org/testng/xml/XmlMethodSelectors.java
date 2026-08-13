package org.testng.xml;

import java.util.ArrayList;
import java.util.List;

public class XmlMethodSelectors {

  private List<XmlMethodSelector> m_methodSelectors = new ArrayList<>();

  public XmlMethodSelectors() {}

  public List<XmlMethodSelector> getMethodSelectors() {
    return m_methodSelectors;
  }

  public void setMethodSelector(XmlMethodSelector xms) {
    m_methodSelectors.add(xms);
  }

  /**
   * @deprecated Serialization has moved to {@code DefaultXmlWeaver}. This method always uses the
   *     default serializer; it never honoured {@code -Dtestng.xml.weaver} and still does not.
   */
  @Deprecated
  public String toXml(String indent) {
    return DefaultXmlWeaver.asXmlFragment(this, indent);
  }
}
