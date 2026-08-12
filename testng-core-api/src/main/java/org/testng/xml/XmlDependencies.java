package org.testng.xml;

import java.util.Map;
import org.testng.collections.Maps;

public class XmlDependencies {

  private Map<String, String> m_xmlDependencyGroups = Maps.newHashMap();

  public void onGroup(String name, String dependsOn) {
    m_xmlDependencyGroups.put(name, dependsOn);
  }

  public Map<String, String> getDependencies() {
    return m_xmlDependencyGroups;
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
