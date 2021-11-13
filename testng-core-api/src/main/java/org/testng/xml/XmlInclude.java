package org.testng.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;

public class XmlInclude {

  private String m_name;
  private Set<Integer> m_invocationNumbers;
  private final int m_index;
  private String m_description;
  private final Map<String, String> m_parameters = Maps.newHashMap();

  private XmlClass m_xmlClass;

  public XmlInclude() {
    this("", 0);
  }

  public XmlInclude(String n) {
    this(n, 0);
  }

  public XmlInclude(String n, int index) {
    this(n, Lists.newArrayList(), index);
  }

  public XmlInclude(String n, List<Integer> list, int index) {
    m_name = n;
    m_invocationNumbers = new HashSet<>(list);
    m_index = index;
  }

  public void setDescription(String description) {
    m_description = description;
  }

  public void setParameters(Map<String, String> parameters) {
    m_parameters.clear();
    m_parameters.putAll(parameters);
  }

  public String getDescription() {
    return m_description;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public List<Integer> getInvocationNumbers() {
    return new ArrayList<>(m_invocationNumbers);
  }

  public void addInvocationNumbers(List<Integer> invocationNumberList) {
    m_invocationNumbers.addAll(invocationNumberList);
  }

  public int getIndex() {
    return m_index;
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    Properties p = new Properties();
    p.setProperty("name", getName());
    List<Integer> invocationNumbers = getInvocationNumbers();
    if (invocationNumbers != null && invocationNumbers.size() > 0) {
      p.setProperty("invocation-numbers", XmlClass.listToString(invocationNumbers));
    }

    if (!m_parameters.isEmpty()) {
      xsb.push("include", p);
      XmlUtils.dumpParameters(xsb, m_parameters);
      xsb.pop("include");
    } else {
      xsb.addEmptyElement("include", p);
    }

    return xsb.toXML();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_index;
    result = prime * result + ((m_invocationNumbers == null) ? 0 : m_invocationNumbers.hashCode());
    result = prime * result + m_parameters.hashCode();
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return XmlSuite.f();
    if (getClass() != obj.getClass()) return XmlSuite.f();
    XmlInclude other = (XmlInclude) obj;
    // if (m_index != other.m_index)
    // return XmlSuite.f();
    if (m_invocationNumbers == null) {
      if (other.m_invocationNumbers != null) return XmlSuite.f();
    } else if (!m_invocationNumbers.equals(other.m_invocationNumbers)) return XmlSuite.f();
    if (m_name == null) {
      if (other.m_name != null) return XmlSuite.f();
    } else if (!m_name.equals(other.m_name)) return XmlSuite.f();
    if (!m_parameters.equals(other.m_parameters)) {
      return XmlSuite.f();
    }
    return true;
  }

  public void addParameter(String name, String value) {
    m_parameters.put(name, value);
  }

  /**
   * @return the parameters defined in this test tag, and only this test tag. To retrieve the
   *     inherited parameters as well, call {@code getAllParameters()}.
   */
  public Map<String, String> getLocalParameters() {
    return m_parameters;
  }

  /** @return the parameters defined in this tag and the tags above it. */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = Maps.newHashMap();
    if (m_xmlClass != null) {
      result.putAll(m_xmlClass.getAllParameters());
    }
    result.putAll(m_parameters);
    return result;
  }

  public void setXmlClass(XmlClass xmlClass) {
    m_xmlClass = xmlClass;
  }
}
