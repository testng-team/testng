package org.testng.xml;

/** This class describes the tag <code>&lt;method-selector&gt;</code> in testng.xml. */
public class XmlMethodSelector {

  /** The priority assumed when the {@code priority} attribute is absent from the suite file. */
  public static final int DEFAULT_PRIORITY = 0;

  // Either this:
  private String m_className;
  private int m_priority = DEFAULT_PRIORITY;

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
    result = prime * result + (m_className == null ? 0 : m_className.hashCode());
    if (getScript() != null) {
      result =
          prime * result
              + (getScript().getExpression() == null ? 0 : getScript().getExpression().hashCode());
      result =
          prime * result
              + (getScript().getLanguage() == null ? 0 : getScript().getLanguage().hashCode());
    }
    return prime * result + m_priority;
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
    XmlMethodSelector other = (XmlMethodSelector) obj;
    if (m_className == null) {
      if (other.m_className != null) {
        return XmlSuite.f();
      }
    } else if (!m_className.equals(other.m_className)) {
      return XmlSuite.f();
    }
    if (getScript() == null || getScript().getExpression() == null) {
      if (other.getScript() != null && other.getScript().getExpression() != null) {
        return XmlSuite.f();
      }
    } else if (!getScript()
        .getExpression()
        .equals(other.getScript() == null ? null : other.getScript().getExpression())) {
      return XmlSuite.f();
    }
    if (getScript() == null || getScript().getLanguage() == null) {
      if (other.getScript() != null && other.getScript().getLanguage() != null) {
        return XmlSuite.f();
      }
    } else if (!getScript()
        .getLanguage()
        .equals(other.getScript() == null ? null : other.getScript().getLanguage())) {
      return XmlSuite.f();
    }
    if (m_priority != other.m_priority) {
      return XmlSuite.f();
    }
    return true;
  }
}
