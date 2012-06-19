package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import com.google.inject.internal.Maps;

import org.testng.reporters.XMLStringBuffer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

public class XmlDependencies {

  private Map<String, String> m_xmlDependencyGroups = Maps.newHashMap();

  public void setGroupNode(Node group) {
    Element e = (Element) group;
    m_xmlDependencyGroups.put(e.getAttribute("name"), e.getAttribute("depends-on"));
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    boolean hasElements = hasElements(m_xmlDependencyGroups);
    if (hasElements) {
      xsb.push("dependencies");
    }
    for (Map.Entry<String, String> entry : m_xmlDependencyGroups.entrySet()) {
      xsb.addEmptyElement("include", "name", entry.getKey(), "depends-on", entry.getValue());
    }
    if (hasElements) {
      xsb.pop("dependencies");
    }

    return xsb.toXML();
  }

}
