package org.testng.xml;

import org.testng.TestNGException;
import org.testng.reporters.XMLStringBuffer;

import java.util.Properties;

/**
 * This class describes the tag <method-selector>  in testng.xml.
 *
 * Created on Sep 26, 2005
 * @author cbeust
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlMethodSelector {
  // Either this:
  private String m_className;
  private int m_priority;

  // Or that:
  private String m_language;
  private String m_expression;

  public String getClassName() {
    return m_className;
  }

  // For YAML
  public void setClassName(String name) {
    setName(name);
  }

  public void setName(String name) {
    m_className = name;
  }

  /**
   * @return Returns the expression.
   */
  public String getExpression() {
    return m_expression;
  }

  /**
   * @param expression The expression to set.
   */
  public void setExpression(String expression) {
    m_expression = expression;
  }

  /**
   * @return Returns the language.
   */
  public String getLanguage() {
    return m_language;
  }

  /**
   * @param language The language to set.
   */
  public void setLanguage(String language) {
    m_language = language;
  }

  public int getPriority() {
    return m_priority;
  }

  public void setPriority(int priority) {
    m_priority = priority;
  }

  private void ppp(String s) {
    System.out.println("[XmlMethodSelector] " + s);
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);

    xsb.push("method-selector");

    if (null != m_className) {
      Properties clsProp = new Properties();
      clsProp.setProperty("name", getClassName());
      if(getPriority() != -1) {
        clsProp.setProperty("priority", String.valueOf(getPriority()));
      }
      xsb.addEmptyElement("selector-class", clsProp);
    }
    else if (getLanguage() != null) {
      Properties scriptProp = new Properties();
      scriptProp.setProperty("language", getLanguage());
      xsb.push("script", scriptProp);
      xsb.addCDATA(getExpression());
      xsb.pop("script");
    }
    else {
      throw new TestNGException("Invalid Method Selector:  found neither class name nor language");
    }

    xsb.pop("method-selector");

    return xsb.toXML();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((m_className == null) ? 0 : m_className.hashCode());
    result = prime * result
        + ((m_expression == null) ? 0 : m_expression.hashCode());
    result = prime * result
        + ((m_language == null) ? 0 : m_language.hashCode());
    result = prime * result + m_priority;
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
    XmlMethodSelector other = (XmlMethodSelector) obj;
    if (m_className == null) {
      if (other.m_className != null)
        return XmlSuite.f();
    } else if (!m_className.equals(other.m_className))
      return XmlSuite.f();
    if (m_expression == null) {
      if (other.m_expression != null)
        return XmlSuite.f();
    } else if (!m_expression.equals(other.m_expression))
      return XmlSuite.f();
    if (m_language == null) {
      if (other.m_language != null)
        return XmlSuite.f();
    } else if (!m_language.equals(other.m_language))
      return XmlSuite.f();
    if (m_priority != other.m_priority)
      return XmlSuite.f();
    return true;
  }
}
