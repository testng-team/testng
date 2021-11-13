package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import java.util.List;
import org.testng.collections.Lists;
import org.testng.reporters.XMLStringBuffer;

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

  //  public void setDependencies(List<XmlDependencies> dependencies) {
  //    m_dependencies = dependencies;
  //  }

  public void setXmlDependencies(XmlDependencies dependencies) {
    m_dependencies.add(dependencies);
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    String indent2 = indent + "  ";

    boolean hasGroups = hasElements(m_defines) || m_run != null || hasElements(m_dependencies);

    if (hasGroups) {
      xsb.push("groups");
    }

    for (XmlDefine d : m_defines) {
      xsb.getStringBuffer().append(d.toXml(indent2));
    }

    if (null != m_run) {
      // XmlRun is optional and is not always available. So check if its available before running
      // toXml()
      xsb.getStringBuffer().append(m_run.toXml(indent2));
    }

    for (XmlDependencies d : m_dependencies) {
      xsb.getStringBuffer().append(d.toXml(indent2));
    }

    if (hasGroups) {
      xsb.pop("groups");
    }

    return xsb.toXML();
  }
}
