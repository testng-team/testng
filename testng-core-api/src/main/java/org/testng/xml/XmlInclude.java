package org.testng.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlInclude {

  private String m_name;
  private final Set<Integer> m_invocationNumbers;
  private final Set<Integer> m_factoryInstances = new HashSet<>();
  private final int m_index;
  private String m_description;
  private final Map<String, String> m_parameters = new HashMap<>();

  private XmlClass m_xmlClass;

  public XmlInclude() {
    this("", 0);
  }

  public XmlInclude(String n) {
    this(n, 0);
  }

  public XmlInclude(String n, int index) {
    this(n, new ArrayList<>(), index);
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

  /**
   * @return - The indexes of the <code>@Factory</code> produced instances this method should run
   *     on, as read from the <code>&lt;include factory-instances="..."&gt;</code> attribute. An
   *     empty list -- the usual case -- means every instance. The indexes are the ones {@link
   *     org.testng.IFactoryInstance#getIndex()} reports.
   *     <p>This is a different axis from {@link #getInvocationNumbers()}, which selects rows of the
   *     test method's own data provider. A factory powered method can be filtered on both.
   * @since 7.13.0
   */
  public List<Integer> getFactoryInstances() {
    return new ArrayList<>(m_factoryInstances);
  }

  /** @since 7.13.0 */
  public void addFactoryInstances(List<Integer> factoryInstanceList) {
    m_factoryInstances.addAll(factoryInstanceList);
  }

  public int getIndex() {
    return m_index;
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
    result = prime * result + m_index;
    result = prime * result + (m_invocationNumbers == null ? 0 : m_invocationNumbers.hashCode());
    result = prime * result + m_factoryInstances.hashCode();
    result = prime * result + m_parameters.hashCode();
    return prime * result + (m_name == null ? 0 : m_name.hashCode());
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
    XmlInclude other = (XmlInclude) obj;
    if (m_invocationNumbers == null) {
      if (other.m_invocationNumbers != null) {
        return XmlSuite.f();
      }
    } else if (!m_invocationNumbers.equals(other.m_invocationNumbers)) {
      return XmlSuite.f();
    }
    if (!m_factoryInstances.equals(other.m_factoryInstances)) {
      return XmlSuite.f();
    }
    if (m_name == null) {
      if (other.m_name != null) {
        return XmlSuite.f();
      }
    } else if (!m_name.equals(other.m_name)) {
      return XmlSuite.f();
    }
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
    Map<String, String> result = new HashMap<>();
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
