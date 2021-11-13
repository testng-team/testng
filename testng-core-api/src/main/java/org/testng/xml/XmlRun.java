package org.testng.xml;

import static org.testng.collections.CollectionUtils.hasElements;

import java.util.List;
import org.testng.collections.Lists;
import org.testng.reporters.XMLStringBuffer;

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

  public List<String> getExcludes() {
    return m_excludes;
  }

  public void onExclude(String name) {
    m_excludes.add(name);
  }

  private List<String> m_includes = Lists.newArrayList();

  public List<String> getIncludes() {
    return m_includes;
  }

  public void onInclude(String name) {
    m_includes.add(name);
  }
}
