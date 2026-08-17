package org.testng.xml;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

public class XmlGroups {

  private List<XmlDefine> m_defines = new ArrayList<>();
  private @Nullable XmlRun m_run;
  private List<XmlDependencies> m_dependencies = new ArrayList<>();

  public List<XmlDefine> getDefines() {
    return m_defines;
  }

  public void addDefine(XmlDefine define) {
    getDefines().add(define);
  }

  public void setDefines(List<XmlDefine> defines) {
    m_defines = defines;
  }

  /**
   * @return the {@code <run>} element, or {@code null} when none has been set. A {@code <groups>}
   *     can legitimately carry only {@code <define>} or {@code <dependencies>} elements.
   */
  public @Nullable XmlRun getRun() {
    return m_run;
  }

  public void setRun(@Nullable XmlRun run) {
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
