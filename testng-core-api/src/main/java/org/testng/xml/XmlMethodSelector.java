package org.testng.xml;

import java.util.Properties;
import org.testng.TestNGException;
import org.testng.reporters.XMLStringBuffer;

/** This class describes the tag <code>&lt;method-selector&gt;</code> in testng.xml. */
public class XmlMethodSelector {
  // Either this:
  private String m_className;
  private int m_priority;

  // Or that:
  private XmlScript m_script;

  // For YAML
  public void setClassName(String s) {
    m_className = s;
  }

  public String getClassName() {
    return m_className;
  }

  // For YAML
  public void setElement(String name, String priority) {
    setName(name);
    setPriority(Integer.parseInt(priority));
  }

  public void setName(String name) {
    m_className = name;
  }

  public XmlScript getScript() {
    return m_script;
  }

  public void setScript(XmlScript script) {
    m_script = script;
  }

  public int getPriority() {
    return m_priority;
  }

  public void setPriority(int priority) {
    m_priority = priority;
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);

    xsb.push("method-selector");

    if (null != m_className) {
      Properties clsProp = new Properties();
      clsProp.setProperty("name", getClassName());
      if (getPriority() != -1) {
        clsProp.setProperty("priority", String.valueOf(getPriority()));
      }
      xsb.addEmptyElement("selector-class", clsProp);
    } else if (getScript() != null && getScript().getLanguage() != null) {
      Properties scriptProp = new Properties();
      scriptProp.setProperty("language", getScript().getLanguage());
      xsb.push("script", scriptProp);
      xsb.addCDATA(getScript().getExpression());
      xsb.pop("script");
    } else {
      throw new TestNGException("Invalid Method Selector:  found neither class name nor language");
    }

    xsb.pop("method-selector");

    return xsb.toXML();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_className == null) ? 0 : m_className.hashCode());
    if (getScript() != null) {
      result =
          prime * result
              + ((getScript().getExpression() == null)
                  ? 0
                  : getScript().getExpression().hashCode());
      result =
          prime * result
              + ((getScript().getLanguage() == null) ? 0 : getScript().getLanguage().hashCode());
    }
    result = prime * result + m_priority;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return XmlSuite.f();
    if (getClass() != obj.getClass()) return XmlSuite.f();
    XmlMethodSelector other = (XmlMethodSelector) obj;
    if (m_className == null) {
      if (other.m_className != null) return XmlSuite.f();
    } else if (!m_className.equals(other.m_className)) return XmlSuite.f();
    if (getScript() == null || getScript().getExpression() == null) {
      if (other.getScript() != null && other.getScript().getExpression() != null)
        return XmlSuite.f();
    } else if (!getScript()
        .getExpression()
        .equals(other.getScript() == null ? null : other.getScript().getExpression())) {
      return XmlSuite.f();
    }
    if (getScript() == null || getScript().getLanguage() == null) {
      if (other.getScript() != null && other.getScript().getLanguage() != null) return XmlSuite.f();
    } else if (!getScript()
        .getLanguage()
        .equals(other.getScript() == null ? null : other.getScript().getLanguage())) {
      return XmlSuite.f();
    }
    if (m_priority != other.m_priority) return XmlSuite.f();
    return true;
  }
}
