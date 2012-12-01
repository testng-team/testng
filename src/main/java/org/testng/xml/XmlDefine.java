package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import org.testng.collections.Lists;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.dom.OnElement;

import java.util.List;

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

  @OnElement(tag = "include", attributes = "name")
  public void onElement(String name) {
    m_includes.add(name);
  }

  public List<String> getIncludes() {
    return m_includes;
  }
}
