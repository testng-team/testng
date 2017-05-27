package org.testng.xml;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.testng.collections.Lists;
import org.testng.internal.PackageUtils;
import org.testng.internal.Utils;
import org.testng.reporters.XMLStringBuffer;

/**
 * This class describes the tag <package>  in testng.xml.
 *
 * @author Cedric
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlPackage implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1996341670354923204L;
  private String m_name;
  private List<String> m_include = Lists.newArrayList();
  private List<String> m_exclude = Lists.newArrayList();
  private List<XmlClass> m_xmlClasses= null;

  public XmlPackage() {
  }

  // For YAML
  public XmlPackage(String name) {
    m_name = name;
  }

  /**
   * @return the exclude
   */
  public List<String> getExclude() {
    return m_exclude;
  }

  /**
   * @param exclude the exclude to set
   */
  public void setExclude(List<String> exclude) {
    m_exclude = exclude;
  }

  /**
   * @return the include
   */
  public List<String> getInclude() {
    return m_include;
  }

  /**
   * @param include the include to set
   */
  public void setInclude(List<String> include) {
    m_include = include;
  }

  /**
   * @return the name
   */
  public String getName() {
    return m_name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    m_name = name;
  }

  public List<XmlClass> getXmlClasses() {
    if(null == m_xmlClasses) {
      m_xmlClasses= initializeXmlClasses();
    }

    return m_xmlClasses;
  }

  private List<XmlClass> initializeXmlClasses() {
    List<XmlClass> result= Lists.newArrayList();
    try {
      String[] classes = PackageUtils.findClassesInPackage(m_name, m_include, m_exclude);

      int index = 0;
      for(String className: classes) {
        result.add(new XmlClass(className, index++, false /* don't load classes */));
      }
    }
    catch(IOException ioex) {
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
        Properties includeProp= new Properties();
        includeProp.setProperty("name", m);
        xsb.addEmptyElement("include", includeProp);
      }
      for (String m: getExclude()) {
        Properties excludeProp= new Properties();
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
    result = prime * result
        + ((m_xmlClasses == null) ? 0 : m_xmlClasses.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return XmlSuite.f();
    if (getClass() != obj.getClass())
      return XmlSuite.f();
    XmlPackage other = (XmlPackage) obj;
    if (m_exclude == null) {
      if (other.m_exclude != null)
        return XmlSuite.f();
    } else if (!m_exclude.equals(other.m_exclude))
      return XmlSuite.f();
    if (m_include == null) {
      if (other.m_include != null)
        return XmlSuite.f();
    } else if (!m_include.equals(other.m_include))
      return XmlSuite.f();
    if (m_name == null) {
      if (other.m_name != null)
        return XmlSuite.f();
    } else if (!m_name.equals(other.m_name))
      return XmlSuite.f();
    if (m_xmlClasses == null) {
      if (other.m_xmlClasses != null)
        return XmlSuite.f();
    } else if (!m_xmlClasses.equals(other.m_xmlClasses))
      return XmlSuite.f();
    return true;
  }

}
