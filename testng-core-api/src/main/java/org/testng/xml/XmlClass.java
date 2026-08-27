package org.testng.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.testng.TestNGException;
import org.testng.collections.Objects;
import org.testng.internal.ClassHelper;

/** This class describes the tag <code>&lt;class&gt;</code> in testng.xml. */
public class XmlClass implements Cloneable {

  private List<XmlInclude> m_includedMethods = new ArrayList<>();
  private List<String> m_excludedMethods = new ArrayList<>();
  // Assigned by init, which every constructor calls directly: NullAway traces an initializer
  // helper one hop only, so reaching init through a delegating overload stops it seeing this.
  private String m_name;
  private @Nullable Class m_class;
  /** The index of this class in the &lt;test&gt; tag */
  private int m_index;
  /** True if the classes need to be loaded */
  private boolean m_loadClasses = true;

  private Map<String, String> m_parameters = new HashMap<>();
  private @Nullable XmlTest m_xmlTest;

  public XmlClass() {
    init("", null, 0, false /* load classes */);
  }

  public XmlClass(String name) {
    init(name, null, 0, true /* load classes */);
  }

  public XmlClass(String name, boolean loadClasses) {
    init(name, null, 0, loadClasses);
  }

  public XmlClass(Class cls) {
    init(cls.getName(), cls, 0, true);
  }

  public XmlClass(Class cls, boolean loadClasses) {
    init(cls.getName(), cls, 0, loadClasses);
  }

  public XmlClass(String className, int index) {
    init(className, null, index, true /* load classes */);
  }

  public XmlClass(String className, int index, boolean loadClasses) {
    init(className, null, index, loadClasses);
  }

  private void init(String className, @Nullable Class cls, int index, boolean resolveClass) {
    m_name = className;
    m_class = cls;
    m_index = index;

    if (null == m_class && resolveClass) {
      loadClass();
    }
  }

  /** Resolves {@link #m_name}, caches it in {@link #m_class} and hands it back. */
  private Class<?> loadClass() {
    Class<?> cls = ClassHelper.forName(m_name);

    if (null == cls) {
      throw new TestNGException("Cannot find class in classpath: " + m_name);
    }
    m_class = cls;
    return cls;
  }

  /** @return Returns the className. */
  public Class<?> getSupportClass() {
    Class<?> cls = m_class;
    return cls == null ? loadClass() : cls;
  }

  /** @param className The className to set. */
  public void setClass(Class className) {
    m_class = className;
  }

  /** @return Returns the excludedMethods. */
  public List<String> getExcludedMethods() {
    return m_excludedMethods;
  }

  /** @param excludedMethods The excludedMethods to set. */
  public void setExcludedMethods(List<String> excludedMethods) {
    m_excludedMethods = excludedMethods;
  }

  /** @return Returns the includedMethods. */
  public List<XmlInclude> getIncludedMethods() {
    return m_includedMethods;
  }

  /** @param includedMethods The includedMethods to set. */
  public void setIncludedMethods(List<XmlInclude> includedMethods) {
    m_includedMethods = includedMethods;
  }

  /** @return Returns the name. */
  public String getName() {
    return m_name;
  }

  /** @param name The name to set. */
  public void setName(String name) {
    m_name = name;
  }

  /** @return true if the classes need to be loaded. */
  public boolean loadClasses() {
    return m_loadClasses;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("class", m_name).toString();
  }

  /**
   * @deprecated Serialization has moved to {@code DefaultXmlWeaver}. This method always uses the
   *     default serializer; it never honoured {@code -Dtestng.xml.weaver} and still does not.
   */
  @Deprecated
  public String toXml(String indent) {
    return DefaultXmlWeaver.asXmlFragment(this, indent);
  }

  public static String listToString(List<Integer> invocationNumbers) {
    StringBuilder result = new StringBuilder();
    int i = 0;
    for (Integer n : invocationNumbers) {
      if (i++ > 0) {
        result.append(" ");
      }
      result.append(n);
    }
    return result.toString();
  }

  /** Clone an XmlClass by copying all its components. */
  @Override
  public Object clone() {
    XmlClass result = new XmlClass(getName(), getIndex(), loadClasses());
    result.setExcludedMethods(getExcludedMethods());
    result.setIncludedMethods(getIncludedMethods());

    return result;
  }

  /**
   * Note that this attribute does not come from the XML file, it's calculated internally and
   * represents the order in which this class was found in its &lt;test&gt; tag. It's used to
   * calculate the ordering of the classes when preserve-order is true.
   *
   * @return the value
   */
  public int getIndex() {
    return m_index;
  }

  public void setIndex(int index) {
    m_index = index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (m_class == null ? 0 : m_class.hashCode());
    result = prime * result + (m_loadClasses ? 1 : 0);
    result = prime * result + m_index;
    return prime * result + (m_name == null ? 0 : m_name.hashCode());
  }

  @Override
  @SuppressWarnings("EqualsGetClass") // published, subclassable value type; see package-info
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
    XmlClass other = (XmlClass) obj;
    if (other.m_loadClasses != m_loadClasses) {
      return XmlSuite.f();
    }
    if (m_name == null) {
      if (other.m_name != null) {
        return XmlSuite.f();
      }
    } else if (!m_name.equals(other.m_name)) {
      return XmlSuite.f();
    }

    return true;
  }

  public void setParameters(Map<String, String> parameters) {
    m_parameters.clear();
    m_parameters.putAll(parameters);
  }

  /** @return The parameters defined in this test tag and the tags above it. */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = new HashMap<>();
    if (m_xmlTest != null) {
      // getAllParameters, not getLocalParameters: "the tags above it" reaches the <suite> too.
      result.putAll(m_xmlTest.getAllParameters());
    }
    result.putAll(m_parameters);
    return result;
  }

  /**
   * @return The parameters defined in this tag, and only this test tag. To retrieve the inherited
   *     parameters as well, call {@code getAllParameters()}.
   */
  public Map<String, String> getLocalParameters() {
    return m_parameters;
  }

  public void setXmlTest(XmlTest test) {
    m_xmlTest = test;
  }
}
