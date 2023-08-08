package org.testng.xml;

import java.util.Properties;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.TestNGException;
import org.testng.reporters.XMLStringBuffer;

/** This class describes the tag <code>&lt;method-selector&gt;</code> in testng.xml. */
public class XmlMethodSelector {
  // Either this:
  private @Nullable String m_className;
  private int m_priority;

  // Or that:
  private @Nullable XmlScript m_script;

  // For YAML
  public void setClassName(@Nullable String s) {
    m_className = s;
  }

  public @Nullable String getClassName() {
    return m_className;
  }

  // For YAML
  public void setElement(@Nullable String name, String priority) {
    setName(name);
    setPriority(Integer.parseInt(priority));
  }

  public void setName(@Nullable String name) {
    m_className = name;
  }

  public @Nullable XmlScript getScript() {
    return m_script;
  }

  public void setScript(@Nullable XmlScript script) {
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
      clsProp.setProperty("name", m_className);
      if (getPriority() != -1) {
        clsProp.setProperty("priority", String.valueOf(getPriority()));
      }
      xsb.addEmptyElement("selector-class", clsProp);
    } else {
      @Nullable XmlScript script = getScript();
      if (script == null) {
        throw new TestNGException("Invalid Method Selector: found neither class name nor script");
      }
      @Nullable String language = script.getLanguage();
      @Nullable String expression = script.getExpression();
      if (language == null || expression == null) {
        throw new TestNGException("Invalid Method Selector: found neither expression nor language");
      }
      Properties scriptProp = new Properties();
      scriptProp.setProperty("language", language);
      xsb.push("script", scriptProp);
      xsb.addCDATA(expression);
      xsb.pop("script");
    }

    xsb.pop("method-selector");

    return xsb.toXML();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_className == null) ? 0 : m_className.hashCode());
    @Nullable XmlScript script = getScript();
    if (script != null) {
      @Nullable String expression = script.getExpression();
      @Nullable String language = script.getLanguage();
      result = prime * result + ((expression == null) ? 0 : expression.hashCode());
      result = prime * result + ((language == null) ? 0 : language.hashCode());
    }
    result = prime * result + m_priority;
    return result;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    XmlMethodSelector other = (XmlMethodSelector) obj;
    if (m_className == null) {
      if (other.m_className != null) return false;
    } else if (!m_className.equals(other.m_className)) return false;
    @Nullable XmlScript script = getScript();
    @Nullable XmlScript otherScript = other.getScript();
    if (script == null || script.getExpression() == null) {
      if (otherScript != null && otherScript.getExpression() != null) return false;
    } else if (!script
        .getExpression()
        .equals(otherScript == null ? null : otherScript.getExpression())) {
      return false;
    }
    if (script == null || script.getLanguage() == null) {
      if (otherScript != null && otherScript.getLanguage() != null) return false;
    } else if (!script
        .getLanguage()
        .equals(otherScript == null ? null : otherScript.getLanguage())) {
      return false;
    }
    if (m_priority != other.m_priority) return false;
    return true;
  }
}
