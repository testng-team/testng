package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import org.testng.collections.Lists;
import org.testng.reporters.XMLStringBuffer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class XmlRun {

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    boolean hasElements = hasElements(m_excludes) || hasElements(m_includes);
    if (hasElements) {
      xsb.push("run");
    }
    for (String s : m_includes) {
      xsb.addEmptyElement("include", "name", s);
    }
    for (String s : m_excludes) {
      xsb.addEmptyElement("exclude", "name", s);
    }
    if (hasElements) {
      xsb.pop("run");
    }

    return xsb.toXML();
  }

  private List<String> m_excludes = Lists.newArrayList();

  public void setExcludeNode(Node node) {
    m_excludes.add(((Element) node).getAttribute("name"));
  }

  private List<String> m_includes = Lists.newArrayList();

  public void setIncludeNode(Node node) {
    m_includes.add(((Element) node).getAttribute("name"));
  }
}
