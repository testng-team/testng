package org.testng.xml;

import java.util.ArrayList;
import java.util.List;

public class XmlDefine {

  private String m_name;

  public void setName(String name) {
    m_name = name;
  }

  public String getName() {
    return m_name;
  }

  /**
   * @deprecated Serialization has moved to {@code DefaultXmlWeaver}. This method always uses the
   *     default serializer; it never honoured {@code -Dtestng.xml.weaver} and still does not.
   */
  @Deprecated
  public String toXml(String indent) {
    return DefaultXmlWeaver.asXmlFragment(this, indent);
  }

  private List<String> m_includes = new ArrayList<>();

  public void onElement(String name) {
    m_includes.add(name);
  }

  public List<String> getIncludes() {
    return m_includes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    XmlDefine define = (XmlDefine) o;
    if (m_name != null ? !m_name.equals(define.m_name) : define.m_name != null) {
      return false;
    }
    return m_includes != null ? m_includes.equals(define.m_includes) : define.m_includes == null;
  }

  @Override
  public int hashCode() {
    int result = m_name != null ? m_name.hashCode() : 0;
    return 31 * result + (m_includes != null ? m_includes.hashCode() : 0);
  }
}
