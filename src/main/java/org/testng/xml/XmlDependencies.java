package org.testng.xml;

import java.util.Map;

import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.dom.OnElement;

import static org.testng.collections.CollectionUtils.hasElements;

public class XmlDependencies {

  private Map<String, String> m_xmlDependencyGroups = Maps.newHashMap();

  @OnElement(tag = "group", attributes = { "name", "depends-on" })
  public void onGroup(String name, String dependsOn) {
    m_xmlDependencyGroups.put(name, dependsOn);
  }

  public Map<String, String> getDependencies() {
    return m_xmlDependencyGroups;
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
