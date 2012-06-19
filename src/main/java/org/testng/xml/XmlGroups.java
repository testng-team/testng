package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import com.google.inject.internal.Lists;

import org.testng.reporters.XMLStringBuffer;

import java.util.List;

public class XmlGroups {

  private List<XmlDefine> m_defines = Lists.newArrayList();
  private List<XmlRun> m_runs = Lists.newArrayList();
  private List<XmlDependencies> m_dependencies = Lists.newArrayList();

  public XmlGroups() {
    
  }

  public List<XmlDefine> getDefines() {
    return m_defines;
  }

  public void setDefines(List<XmlDefine> defines) {
    m_defines = defines;
  }

  public List<XmlRun> getRuns() {
    return m_runs;
  }

  public void setRuns(List<XmlRun> runs) {
    m_runs = runs;
  }

  public List<XmlDependencies> getDependencies() {
    return m_dependencies;
  }

  public void setDependencies(List<XmlDependencies> dependencies) {
    m_dependencies = dependencies;
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    String indent2 = indent + "  ";

    boolean hasGroups = hasElements(m_defines) || hasElements(m_runs)
        || hasElements(m_dependencies);

    if (hasGroups) {
      xsb.push("groups");
    }

    for (XmlDefine d : m_defines) {
      xsb.getStringBuffer().append(d.toXml(indent2));
    }

    for (XmlRun d : m_runs) {
      xsb.getStringBuffer().append(d.toXml(indent2));
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
