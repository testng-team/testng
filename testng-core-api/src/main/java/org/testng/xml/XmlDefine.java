package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import java.util.List;
import org.testng.collections.Lists;
import org.testng.reporters.XMLStringBuffer;

public class XmlDefine {

  private String m_name;

  public void setName(String name) {
    m_name = name;
  }

  public String getName() {
    return m_name;
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    boolean hasElements = hasElements(m_includes);
    if (hasElements) {
      xsb.push("define", "name", m_name);
    }
    for (String s : m_includes) {
      xsb.addEmptyElement("include", "name", s);
    }
    if (hasElements) {
      xsb.pop("define");
    }

    return xsb.toXML();
  }

  private List<String> m_includes = Lists.newArrayList();

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
    result = 31 * result + (m_includes != null ? m_includes.hashCode() : 0);
    return result;
  }
}
