package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import java.util.Map;
import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;

public class XmlDependencies {

  private Map<String, String> m_xmlDependencyGroups = Maps.newHashMap();

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
