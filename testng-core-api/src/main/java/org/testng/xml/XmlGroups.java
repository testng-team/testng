package org.testng.xml;

import java.util.List;
import org.testng.collections.Lists;

public class XmlGroups {

  private List<XmlDefine> m_defines = Lists.newArrayList();
  private XmlRun m_run;
  private List<XmlDependencies> m_dependencies = Lists.newArrayList();

  public List<XmlDefine> getDefines() {
    return m_defines;
  }

  public void addDefine(XmlDefine define) {
    getDefines().add(define);
  }

  public void setDefines(List<XmlDefine> defines) {
    m_defines = defines;
  }

  public XmlRun getRun() {
    return m_run;
  }

  public void setRun(XmlRun run) {
    m_run = run;
  }

  public List<XmlDependencies> getDependencies() {
    return m_dependencies;
  }

  public void setXmlDependencies(XmlDependencies dependencies) {
    m_dependencies.add(dependencies);
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
