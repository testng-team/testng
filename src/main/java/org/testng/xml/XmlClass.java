package org.testng.xml;


import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.reporters.XMLStringBuffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class describes the tag <class> in testng.xml.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 */
public class XmlClass implements Serializable, Cloneable {
  private List<XmlInclude> m_includedMethods = Lists.newArrayList();
  private List<String> m_excludedMethods = Lists.newArrayList();
  private String       m_name = null;
  private Class        m_class = null;
  private Boolean      m_declaredClass = null;
  /** The index of this class in the <test> tag */
  private int m_index;

  public XmlClass(String name) {
    init(name, null, Boolean.TRUE, 0);
  }

  public XmlClass(Class className) {
    init(className.getName(), null, Boolean.TRUE, 0);
  }

  public XmlClass(String name, Boolean declaredClass, int index) {
    init(name, null, declaredClass, index);
  }

  public XmlClass(Class className, Boolean declaredClass) {
    init(className.getName(), className, declaredClass, 0);
  }

  private void init(String name, Class className, Boolean declaredClass, int index) {
    m_name = name;
    m_class = className;
    m_declaredClass = declaredClass;
    m_index = index;
  }

  /**
   * @return Returns the className.
   */
  public Class getSupportClass() {
    if(null == m_class) {
      m_class = ClassHelper.forName(m_name);
      
      if(null == m_class) {
        throw new TestNGException("Cannot find class in classpath: " + m_name);
      }
    }

    return m_class;
  }

  /**
   * @param className The className to set.
   */
  public void setClass(Class className) {
    m_class = className;
  }

  /**
   * @return Returns the excludedMethods.
   */
  public List<String> getExcludedMethods() {
    return m_excludedMethods;
  }

  /**
   * @param excludedMethods The excludedMethods to set.
   */
  public void setExcludedMethods(List<String> excludedMethods) {
    m_excludedMethods = excludedMethods;
  }

  /**
   * @return Returns the includedMethods.
   */
  public List<XmlInclude> getIncludedMethods() {
    return m_includedMethods;
  }

  /**
   * @param includedMethods The includedMethods to set.
   */
  public void setIncludedMethods(List<XmlInclude> includedMethods) {
    m_includedMethods = includedMethods;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return m_name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    m_name = name;
  }

  public Boolean getDeclaredClass() {
    return m_declaredClass;
  }

  public void setDeclaredClass(Boolean declaredClass) {
    this.m_declaredClass = declaredClass;
  }

  @Override
  public String toString() {
    return "[Class: " + m_name + "]";
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    Properties      pro = new Properties();
    pro.setProperty("name", getName());

    if (!m_includedMethods.isEmpty() || !m_excludedMethods.isEmpty()) {
      xsb.push("class", pro);
      xsb.push("methods");
      
      for (XmlInclude m : getIncludedMethods()) {
        xsb.getStringBuffer().append(m.toXml(indent + "    "));
      }

      for (String m: getExcludedMethods()) {
        Properties p= new Properties();
        p.setProperty("name", m);
        xsb.addEmptyElement("exclude", p);
      }
      
      xsb.pop("methods");
      xsb.pop("class");
    }
    else {
      xsb.addEmptyElement("class", pro);
    }

    return xsb.toXML();

  }
  
  public static String listToString(List<Integer> invocationNumbers) {
    StringBuilder result = new StringBuilder();
    int i = 0;
    for (Integer n : invocationNumbers) {
      if (i++ > 0) result.append(" ");
      result.append(n);
    }
    return result.toString();
  }

  /**
   * Clone an XmlClass by copying all its components.
   */
  @Override
  public Object clone() {
    XmlClass result = new XmlClass(getName(), getDeclaredClass(), getIndex());
    result.setExcludedMethods(getExcludedMethods());
    result.setIncludedMethods(getIncludedMethods());
    
    return result;
  }

  /**
   * Note that this attribute does not come from the XML file, it's calculated
   * internally and represents the order in which this class was found in its
   * &lt;test&gt; tag.  It's used to calculate the ordering of the classes
   * when preserve-order is true.
   */
  public int getIndex() {
    return m_index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_class == null) ? 0 : m_class.hashCode());
    result = prime * result
        + ((m_declaredClass == null) ? 0 : m_declaredClass.hashCode());
    result = prime * result
        + ((m_excludedMethods == null) ? 0 : m_excludedMethods.hashCode());
    result = prime * result
        + ((m_includedMethods == null) ? 0 : m_includedMethods.hashCode());
    result = prime * result + m_index;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    XmlClass other = (XmlClass) obj;
    if (m_class == null) {
      if (other.m_class != null)
        return false;
    } else if (!m_class.equals(other.m_class))
      return false;
    if (m_declaredClass == null) {
      if (other.m_declaredClass != null)
        return false;
    } else if (!m_declaredClass.equals(other.m_declaredClass))
      return false;
    if (m_excludedMethods == null) {
      if (other.m_excludedMethods != null)
        return false;
    } else if (!m_excludedMethods.equals(other.m_excludedMethods))
      return false;
    if (m_includedMethods == null) {
      if (other.m_includedMethods != null)
        return false;
    } else if (!m_includedMethods.equals(other.m_includedMethods))
      return false;
    if (m_index != other.m_index)
      return false;
    if (m_name == null) {
      if (other.m_name != null)
        return false;
    } else if (!m_name.equals(other.m_name))
      return false;
    return true;
  }

}
