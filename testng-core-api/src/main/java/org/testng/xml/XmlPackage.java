package org.testng.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.internal.PackageUtils;
import org.testng.internal.Utils;
import org.testng.internal.protocols.UnhandledIOException;

/** This class describes the tag <code>&lt;package&gt;</code> in testng.xml. */
public class XmlPackage {

  private @Nullable String m_name;
  private List<String> m_include = new ArrayList<>();
  private List<String> m_exclude = new ArrayList<>();
  private @Nullable List<XmlClass> m_xmlClasses;

  public XmlPackage() {}

  // For YAML
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
  public @Nullable String getName() {
    return m_name;
  }

  /** @param name the name to set */
  public void setName(@Nullable String name) {
    m_name = name;
  }

  public List<XmlClass> getXmlClasses() {
    List<XmlClass> xmlClasses = m_xmlClasses;
    if (null == xmlClasses) {
      xmlClasses = initializeXmlClasses();
      m_xmlClasses = xmlClasses;
    }

    return xmlClasses;
  }

  private List<XmlClass> initializeXmlClasses() {
    List<XmlClass> result = new ArrayList<>();
    String name = m_name;
    if (name == null) {
      // A <package> tag carrying no name attribute. Reported the same way as an unreadable
      // package below, rather than through the NullPointerException this used to raise.
      Utils.log("XmlPackage", 1, "Ignoring a <package> tag that carries no name.");
      return result;
    }
    try {
      String[] classes = PackageUtils.findClassesInPackage(name, m_include, m_exclude);

      int index = 0;
      for (String className : classes) {
        result.add(new XmlClass(className, index++, false /* don't load classes */));
      }
    } catch (IOException | UnhandledIOException ioex) {
      Utils.log("XmlPackage", 1, ioex.getMessage());
    }

    return result;
  }

  /**
   * @deprecated Serialization has moved to {@code DefaultXmlWeaver}. This method always uses the
   *     default serializer; it never honoured {@code -Dtestng.xml.weaver} and still does not.
   */
  @Deprecated
  public String toXml(String indent) {
    return DefaultXmlWeaver.asXmlFragment(this, indent);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_exclude == null ? 0 : m_exclude.hashCode());
    result = prime * result + (m_include == null ? 0 : m_include.hashCode());
    result = prime * result + (m_name == null ? 0 : m_name.hashCode());
    return prime * result + (m_xmlClasses == null ? 0 : m_xmlClasses.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return XmlSuite.f();
    }
    if (getClass() != obj.getClass()) {
      return XmlSuite.f();
    }
    XmlPackage other = (XmlPackage) obj;
    if (m_exclude == null) {
      if (other.m_exclude != null) {
        return XmlSuite.f();
      }
    } else if (!m_exclude.equals(other.m_exclude)) {
      return XmlSuite.f();
    }
    if (m_include == null) {
      if (other.m_include != null) {
        return XmlSuite.f();
      }
    } else if (!m_include.equals(other.m_include)) {
      return XmlSuite.f();
    }
    if (m_name == null) {
      if (other.m_name != null) {
        return XmlSuite.f();
      }
    } else if (!m_name.equals(other.m_name)) {
      return XmlSuite.f();
    }
    if (m_xmlClasses == null) {
      if (other.m_xmlClasses != null) {
        return XmlSuite.f();
      }
    } else if (!m_xmlClasses.equals(other.m_xmlClasses)) {
      return XmlSuite.f();
    }
    return true;
  }
}
