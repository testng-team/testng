package org.testng.xml;

import java.util.ArrayList;
import java.util.List;

public class XmlRun {

  /**
   * @deprecated Serialization has moved to {@code DefaultXmlWeaver}. This method always uses the
   *     default serializer; it never honoured {@code -Dtestng.xml.weaver} and still does not.
   */
  @Deprecated
  public String toXml(String indent) {
    return DefaultXmlWeaver.asXmlFragment(this, indent);
  }

  private List<String> m_excludes = new ArrayList<>();

  public List<String> getExcludes() {
    return m_excludes;
  }

  public void onExclude(String name) {
    m_excludes.add(name);
  }

  private List<String> m_includes = new ArrayList<>();

  public List<String> getIncludes() {
    return m_includes;
  }

  public void onInclude(String name) {
    m_includes.add(name);
  }
}
