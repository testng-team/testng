package org.testng.xml;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.collections.Lists;
import org.testng.internal.PackageUtils;
import org.testng.internal.Utils;
import org.testng.internal.protocols.UnhandledIOException;
import org.testng.reporters.XMLStringBuffer;

/** This class describes the tag <code>&lt;package&gt;</code> in testng.xml. */
public class XmlPackage {

  private String m_name;
  private List<String> m_include = Lists.newArrayList();
  private List<String> m_exclude = Lists.newArrayList();
  private @Nullable List<XmlClass> m_xmlClasses = null;

  @SuppressWarnings("initialization.fields.uninitialized")
  // For YAML
  public XmlPackage() {}

  public XmlPackage(String name) {
    m_name = name;
  }

  /** @return the exclude */
  public List<String> getExclude() {
    return m_exclude;
  }

  /** @param exclude the exclude to set */
  public void setExclude(List<String> exclude) {
    m_exclude = exclude;
  }

  /** @return the include */
  public List<String> getInclude() {
    return m_include;
  }

  /** @param include the include to set */
  public void setInclude(List<String> include) {
    m_include = include;
  }

  /** @return the name */
  public String getName() {
    return m_name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    m_name = name;
  }

  public List<XmlClass> getXmlClasses() {
    if (null == m_xmlClasses) {
      m_xmlClasses = initializeXmlClasses();
    }

    return m_xmlClasses;
  }

  private List<XmlClass> initializeXmlClasses() {
    List<XmlClass> result = Lists.newArrayList();
    try {
      String[] classes = PackageUtils.findClassesInPackage(m_name, m_include, m_exclude);

      int index = 0;
      for (String className : classes) {
        result.add(new XmlClass(className, index++, false /* don't load classes */));
      }
    } catch (IOException | UnhandledIOException ioex) {
      Utils.log("XmlPackage", 1, ioex.getMessage());
    }

    return result;
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    Properties p = new Properties();
    p.setProperty("name", getName());

    if (getInclude().isEmpty() && getExclude().isEmpty()) {
      xsb.addEmptyElement("package", p);
    } else {
      xsb.push("package", p);

      for (String m : getInclude()) {
        Properties includeProp = new Properties();
        includeProp.setProperty("name", m);
        xsb.addEmptyElement("include", includeProp);
      }
      for (String m : getExclude()) {
        Properties excludeProp = new Properties();
        excludeProp.setProperty("name", m);
        xsb.addEmptyElement("exclude", excludeProp);
      }

      xsb.pop("package");
    }

    return xsb.toXML();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_exclude == null) ? 0 : m_exclude.hashCode());
    result = prime * result + ((m_include == null) ? 0 : m_include.hashCode());
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result + ((m_xmlClasses == null) ? 0 : m_xmlClasses.hashCode());
    return result;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    XmlPackage other = (XmlPackage) obj;
    if (m_exclude == null) {
      if (other.m_exclude != null) return false;
    } else if (!m_exclude.equals(other.m_exclude)) return false;
    if (m_include == null) {
      if (other.m_include != null) return false;
    } else if (!m_include.equals(other.m_include)) return false;
    if (m_name == null) {
      if (other.m_name != null) return false;
    } else if (!m_name.equals(other.m_name)) return false;
    if (m_xmlClasses == null) {
      if (other.m_xmlClasses != null) return false;
    } else if (!m_xmlClasses.equals(other.m_xmlClasses)) return false;
    return true;
  }
}
