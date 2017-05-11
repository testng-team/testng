package org.testng.xml;

import org.testng.TestNGException;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.dom.OnElement;

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
  private XmlScript m_script = new XmlScript();

  // For YAML
  public void setClassName(String s) {
    m_className = s;
  }

  public String getClassName() {
    return m_className;
  }

  // For YAML
  @OnElement(tag = "selector-class", attributes = { "name", "priority" })
  public void setElement(String name, String priority) {
    setName(name);
    setPriority(Integer.parseInt(priority));
  }

  public void setName(String name) {
    m_className = name;
  }

  public void setScript(XmlScript script) {
    m_script = script;
  }

  /**
   * @return Returns the expression.
   */
  public String getExpression() {
    return m_script.getScript();
  }

  /**
   * @param expression The expression to set.
   */
  public void setExpression(String expression) {
    m_script.setScript(expression);
  }

  /**
   * @return Returns the language.
   */
  public String getLanguage() {
    return m_script.getLanguage();
  }

  /**
   * @param language The language to set.
   */
//  @OnElement(tag = "script", attributes = "language")
  public void setLanguage(String language) {
    m_script.setLanguage(language);
//    m_language = language;
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
        + ((getExpression() == null) ? 0 : getExpression().hashCode());
    result = prime * result
        + ((getLanguage() == null) ? 0 : getLanguage().hashCode());
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
    if (getExpression() == null) {
      if (other.getExpression() != null)
        return XmlSuite.f();
    } else if (!getExpression().equals(other.getExpression()))
      return XmlSuite.f();
    if (getLanguage() == null) {
      if (other.getLanguage() != null)
        return XmlSuite.f();
    } else if (!getLanguage().equals(other.getLanguage()))
      return XmlSuite.f();
    if (m_priority != other.m_priority)
      return XmlSuite.f();
    return true;
  }
}
