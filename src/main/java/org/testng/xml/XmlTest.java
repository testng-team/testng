package org.testng.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.dom.ParentSetter;

/**
 * This class describes the tag &lt;test&gt;  in testng.xml.
 *
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 * @author <a href = 'mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlTest implements Serializable, Cloneable {
  private static final long serialVersionUID = 6533504325942417606L;

  public static final int DEFAULT_TIMEOUT_MS = Integer.MAX_VALUE;

  private XmlSuite m_suite;
  private String m_name;
  private Integer m_verbose = XmlSuite.DEFAULT_VERBOSE;
  private Boolean m_isJUnit = XmlSuite.DEFAULT_JUNIT;
  private int m_threadCount= -1;

  private List<XmlClass> m_xmlClasses = Lists.newArrayList();

  private List<String> m_includedGroups = Lists.newArrayList();
  private List<String> m_excludedGroups = Lists.newArrayList();

  private Map<String, List<String>> m_metaGroups = Maps.newHashMap();
  private Map<String, String> m_parameters = Maps.newHashMap();
  private XmlSuite.ParallelMode m_parallel;

  private List<XmlMethodSelector> m_methodSelectors = Lists.newArrayList();
  // test level packages
  private List<XmlPackage> m_xmlPackages = Lists.newArrayList();

  private String m_timeOut;
  private Boolean m_skipFailedInvocationCounts = XmlSuite.DEFAULT_SKIP_FAILED_INVOCATION_COUNTS;
  private Map<String, List<Integer>> m_failedInvocationNumbers = null; // lazily initialized

  private String m_preserveOrder = XmlSuite.DEFAULT_PRESERVE_ORDER;

  private int m_index;

  private Boolean m_groupByInstances;

  private Boolean m_allowReturnValues = null;

  private Map<String, String> m_xmlDependencyGroups = Maps.newHashMap();

  /**
   * Constructs a <code>XmlTest</code> and adds it to suite's list of tests.
   *
   * @param suite the parent suite.
   * @param index the index of this test tag in testng.xml
   */
  public XmlTest(XmlSuite suite, int index) {
    init(suite, index);
  }

  public XmlTest(XmlSuite suite) {
    init(suite, 0);
  }

  private void init(XmlSuite suite, int index) {
    m_suite = suite;
    m_suite.getTests().add(this);
    m_index = index;
    //no two tests in the same suite should have the same name.
    //so, make the default test name unique
    m_name = TestNG.DEFAULT_COMMAND_LINE_TEST_NAME
      + " " + UUID.randomUUID().toString();
  }

  // For YAML
  public XmlTest() {
  }

  public void setXmlPackages(List<XmlPackage> packages) {
    m_xmlPackages = Lists.newArrayList(packages);
  }

  public List<XmlPackage> getXmlPackages() {
    return m_xmlPackages;
  }

  // For YAML
  public List<XmlPackage> getPackages() {
    return getXmlPackages();
  }

  // For YAML
  public void setPackages(List<XmlPackage> p) {
    setXmlPackages(p);
  }

  public List<XmlMethodSelector> getMethodSelectors() {
    return m_methodSelectors;
  }

  public void setMethodSelectors(List<XmlMethodSelector> methodSelectors) {
    m_methodSelectors = Lists.newArrayList(methodSelectors);
  }

  /**
   * Returns the suite this test is part of.
   * @return the suite this test is part of.
   */
  public XmlSuite getSuite() {
    return m_suite;
  }

  /**
   * @return the includedGroups.
   * Note: do not modify the returned value, use {@link #addIncludedGroup(String)}.
   */
  public List<String> getIncludedGroups() {
    List<String> result;
    if (m_xmlGroups != null) {
      result = m_xmlGroups.getRun().getIncludes();
      result.addAll(m_suite.getIncludedGroups());
    } else {
      // deprecated
      result = Lists.newArrayList(m_includedGroups);
      result.addAll(m_suite.getIncludedGroups());
    }
    return result;
  }

  /**
   * Sets the XML Classes.
   * @param classes The classes to set.
   * @deprecated use setXmlClasses
   */
  @Deprecated
  public void setClassNames(List<XmlClass> classes) {
    m_xmlClasses = classes;
  }

  /**
   * @return Returns the classes.
   */
  public List<XmlClass> getXmlClasses() {
    return m_xmlClasses;
  }

  // For YAML
  public List<XmlClass> getClasses() {
    return getXmlClasses();
  }

  // For YAML
  public void setClasses(List<XmlClass> c) {
    setXmlClasses(c);
  }

  /**
   * Sets the XML Classes.
   * @param classes The classes to set.
   */
  public void setXmlClasses(List<XmlClass> classes) {
    m_xmlClasses = classes;
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

  /**
   * @param v
   */
  public void setVerbose(int v) {
    m_verbose = v;
  }

  public int getThreadCount() {
    return m_threadCount > 0 ? m_threadCount : getSuite().getThreadCount();
  }

  public void setThreadCount(int threadCount) {
    m_threadCount = threadCount;
  }

  /**
   * @param g
   */
  public void setIncludedGroups(List<String> g) {
    m_includedGroups = g;
  }

  /**
   * @param g The excludedGrousps to set.
   */
  public void setExcludedGroups(List<String> g) {
    m_excludedGroups = g;
  }

  /**
   * @return Returns the excludedGroups.
   * Note: do not modify the returned value, use {@link #addExcludedGroup(String)}.
   */
  public List<String> getExcludedGroups() {
    List<String> result = new ArrayList(m_excludedGroups);
    result.addAll(m_suite.getExcludedGroups());
    return result;
  }

  public void addIncludedGroup(String g) {
    m_includedGroups.add(g);
  }

  public void addExcludedGroup(String g) {
    m_excludedGroups.add(g);
  }

  /**
   * @return Returns the verbose.
   */
  public int getVerbose() {
    Integer result = m_verbose;
    if (null == result || XmlSuite.DEFAULT_VERBOSE.equals(m_verbose)) {
      result = m_suite.getVerbose();
    }

    if (null != result) {
      return result;
    } else {
      return 1;
    }
  }

  public boolean getGroupByInstances() {
    Boolean result = m_groupByInstances;
    if (result == null || XmlSuite.DEFAULT_GROUP_BY_INSTANCES.equals(m_groupByInstances)) {
      result = m_suite.getGroupByInstances();
    }
    if (result != null) {
      return result;
    } else {
      return XmlSuite.DEFAULT_GROUP_BY_INSTANCES;
    }
  }

  public void setGroupByInstances(boolean f) {
    m_groupByInstances = f;
  }

  /**
   * @return Returns the isJUnit.
   */
  public boolean isJUnit() {
    Boolean result = m_isJUnit;
    if (null == result || XmlSuite.DEFAULT_JUNIT.equals(result)) {
      result = m_suite.isJUnit();
    }

    return result;
  }

  /**
   * @param isJUnit The isJUnit to set.
   */
  public void setJUnit(boolean isJUnit) {
    m_isJUnit = isJUnit;
  }

  // For YAML
  public void setJunit(boolean isJUnit) {
    setJUnit(isJUnit);
  }

  public void setSkipFailedInvocationCounts(boolean skip) {
    m_skipFailedInvocationCounts = skip;
  }

  /**
   * @return Returns the isJUnit.
   */
  public boolean skipFailedInvocationCounts() {
    Boolean result = m_skipFailedInvocationCounts;
    if (null == result) {
      result = m_suite.skipFailedInvocationCounts();
    }

    return result;
  }

  public void addMetaGroup(String name, List<String> metaGroup) {
    m_metaGroups.put(name, metaGroup);
  }

  // For YAML
  public void setMetaGroups(Map<String, List<String>> metaGroups) {
    m_metaGroups = metaGroups;
  }

  /**
   * @return Returns the metaGroups.
   */
  public Map<String, List<String>> getMetaGroups() {
    if (m_xmlGroups != null) {
      Map<String, List<String>> result = Maps.newHashMap();
      List<XmlDefine> defines = m_xmlGroups.getDefines();
      for (XmlDefine xd : defines) {
        result.put(xd.getName(), xd.getIncludes());
      }
      return result;
    } else {
      // deprecated
      return m_metaGroups;
    }
  }

  /**
   * @param parameters
   */
  public void setParameters(Map<String, String> parameters) {
    m_parameters = parameters;
  }

  public void addParameter(String key, String value) {
    m_parameters.put(key, value);
  }

  public String getParameter(String name) {
    String result = m_parameters.get(name);
    if (null == result) {
      result = m_suite.getParameter(name);
    }

    return result;
  }

  /**
   * @return the parameters defined in this test tag and the tags above it.
   */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = Maps.newHashMap();
    result.putAll(getSuite().getParameters());
    result.putAll(m_parameters);
    return result;
  }

  /**
   * @return the parameters defined in this tag, and only this test tag. To retrieve
   * the inherited parameters as well, call {@code getAllParameters()}.
   */
  public Map<String, String> getLocalParameters() {
    return m_parameters;
  }

  /**
   * @deprecated Use {@code getLocalParameters()} or {@code getAllParameters()}
   */
  @Deprecated
  public Map<String, String> getParameters() {
    return getAllParameters();
  }

  /**
   * @return the parameters defined on this <test> tag only
   */
  public Map<String, String> getTestParameters() {
    return m_parameters;
  }

  public void setParallel(XmlSuite.ParallelMode parallel) {
    m_parallel = parallel;
  }

  public XmlSuite.ParallelMode getParallel() {
    XmlSuite.ParallelMode result;
    if (null != m_parallel || XmlSuite.DEFAULT_PARALLEL.equals(m_parallel)) {
      result = m_parallel;
    }
    else {
      result = m_suite.getParallel();
    }

    return result;
  }

  public String getTimeOut() {
    String result = null;
    if (null != m_timeOut) {
      result = m_timeOut;
    }
    else {
      result = m_suite.getTimeOut();
    }

    return result;
  }

  public long getTimeOut(long def) {
    long result = def;
    if (getTimeOut() != null) {
        result = Long.parseLong(getTimeOut());
    }

    return result;
  }

  public void setTimeOut(long timeOut) {
      m_timeOut = Long.toString(timeOut);
  }

  private void setTimeOut(String timeOut) {
      m_timeOut = timeOut;
  }

  public void setExpression(String expression) {
    setBeanShellExpression(expression);
  }

  public void setBeanShellExpression(String expression) {
    List<XmlMethodSelector> selectors = getMethodSelectors();
    if (selectors.size() > 0) {
      selectors.get(0).setExpression(expression);
    } else if (expression != null) {
      XmlMethodSelector xms = new XmlMethodSelector();
      xms.setExpression(expression);
      xms.setLanguage("BeanShell");
      getMethodSelectors().add(xms);
    }
  }

  public String getExpression() {
    List<XmlMethodSelector> selectors = getMethodSelectors();
    if (selectors.size() > 0) {
      return selectors.get(0).getExpression();
    } else {
      return null;
    }
  }

  public String toXml(String indent) {
    XMLStringBuffer xsb = new XMLStringBuffer(indent);
    Properties p = new Properties();
    p.setProperty("name", getName());
    if (m_isJUnit != null) {
      XmlUtils.setProperty(p, "junit", m_isJUnit.toString(), XmlSuite.DEFAULT_JUNIT.toString());
    }
    if (m_parallel != null) {
      XmlUtils.setProperty(p, "parallel", m_parallel.toString(), XmlSuite.DEFAULT_PARALLEL.toString());
    }
    if (m_verbose != null) {
      XmlUtils.setProperty(p, "verbose", m_verbose.toString(), XmlSuite.DEFAULT_VERBOSE.toString());
    }
    if (null != m_timeOut) {
      p.setProperty("time-out", m_timeOut.toString());
    }
    if (m_preserveOrder != null && ! XmlSuite.DEFAULT_PRESERVE_ORDER.equals(m_preserveOrder)) {
      p.setProperty("preserve-order", m_preserveOrder.toString());
    }
    if (m_threadCount != -1) {
      p.setProperty("thread-count", Integer.toString(m_threadCount));
    }
    if (m_groupByInstances != null) {
      XmlUtils.setProperty(p, "group-by-instances", String.valueOf(getGroupByInstances()),
          XmlSuite.DEFAULT_GROUP_BY_INSTANCES.toString());
    }

    xsb.push("test", p);


    if (null != getMethodSelectors() && !getMethodSelectors().isEmpty()) {
      xsb.push("method-selectors");
      for (XmlMethodSelector selector: getMethodSelectors()) {
        xsb.getStringBuffer().append(selector.toXml(indent + "    "));
      }

      xsb.pop("method-selectors");
    }

    XmlUtils.dumpParameters(xsb, m_parameters);

    // groups
    if (!m_metaGroups.isEmpty() || !m_includedGroups.isEmpty() || !m_excludedGroups.isEmpty()
        || !m_xmlDependencyGroups.isEmpty()) {
      xsb.push("groups");

      // define
      for (Map.Entry<String, List<String>> entry: m_metaGroups.entrySet()) {
        String metaGroupName = entry.getKey();
        List<String> groupNames = entry.getValue();

        Properties metaGroupProp= new Properties();
        metaGroupProp.setProperty("name",  metaGroupName);

        xsb.push("define", metaGroupProp);

        for (String groupName: groupNames) {
          Properties includeProps = new Properties();
          includeProps.setProperty("name", groupName);

          xsb.addEmptyElement("include", includeProps);
        }

        xsb.pop("define");
      }

      // run
      if (!m_includedGroups.isEmpty() || !m_excludedGroups.isEmpty()) {
        xsb.push("run");

        for (String includeGroupName: m_includedGroups) {
          Properties includeProps = new Properties();
          includeProps.setProperty("name", includeGroupName);

          xsb.addEmptyElement("include", includeProps);
        }

        for (String excludeGroupName: m_excludedGroups) {
          Properties excludeProps = new Properties();
          excludeProps.setProperty("name", excludeGroupName);

          xsb.addEmptyElement("exclude", excludeProps);
        }

        xsb.pop("run");
      }

      // group dependencies
      if (m_xmlDependencyGroups != null && ! m_xmlDependencyGroups.isEmpty()) {
        xsb.push("dependencies");
        for (Map.Entry<String, String> entry : m_xmlDependencyGroups.entrySet()) {
          xsb.addEmptyElement("group", "name", entry.getKey(), "depends-on", entry.getValue());
        }
        xsb.pop("dependencies");
      }

      xsb.pop("groups");
    }

    if (null != m_xmlPackages && !m_xmlPackages.isEmpty()) {
      xsb.push("packages");

      for (XmlPackage pack: m_xmlPackages) {
        xsb.getStringBuffer().append(pack.toXml("      "));
      }

      xsb.pop("packages");
    }

    // classes
    if (null != getXmlClasses() && !getXmlClasses().isEmpty()) {
      xsb.push("classes");
      for (XmlClass cls : getXmlClasses()) {
        xsb.getStringBuffer().append(cls.toXml(indent + "    "));
      }
      xsb.pop("classes");
    }

    xsb.pop("test");

    return xsb.toXML();
  }

  @Override
  public String toString() {
//    return toXml("");
    StringBuilder result = new StringBuilder("[Test: \"")
            .append(m_name)
            .append("\"")
            .append(" verbose:")
            .append(m_verbose);

    result.append("[parameters:");
    for (Map.Entry<String, String> entry : m_parameters.entrySet()) {
      result.append(entry.getKey()).append("=>").append(entry.getValue());
    }

    result.append("]");
    result.append("[metagroups:");
    for (Map.Entry<String, List<String>> entry : m_metaGroups.entrySet()) {
      result.append(entry.getKey()).append("=");
      for (String n : entry.getValue()) {
        result.append(n).append(",");
      }
    }
    result.append("] ");

    result.append("[included: ");
    for (String g : m_includedGroups) {
      result.append(g).append(" ");
    }
    result.append("]");

    result.append("[excluded: ");
    for (String g : m_excludedGroups) {
      result.append(g).append(" ");
    }
    result.append("] ");

    result.append(" classes:");
    for (XmlClass cl : m_xmlClasses) {
      result.append(cl).append(" ");
    }

    result.append(" packages:");
    for (XmlPackage p : m_xmlPackages) {
      result.append(p).append(" ");
    }

    result.append("] ");

    return result.toString();
  }

  static void ppp(String s) {
    System.out.println("[XmlTest] " + s);
  }

  /**
   * Clone the <TT>source</TT> <CODE>XmlTest</CODE> by including:
   * - test attributes
   * - groups definitions
   * - parameters
   *
   * The &lt;classes&gt; sub element is ignored for the moment.
   *
   * @return a clone of the current XmlTest
   */
  @Override
  public Object clone() {
    XmlTest result = new XmlTest(getSuite());

    result.setName(getName());
    result.setIncludedGroups(getIncludedGroups());
    result.setExcludedGroups(getExcludedGroups());
    result.setJUnit(isJUnit());
    result.setParallel(getParallel());
    result.setVerbose(getVerbose());
    result.setParameters(getLocalParameters());
    result.setXmlPackages(getXmlPackages());
    result.setTimeOut(getTimeOut());

    Map<String, List<String>> metagroups = getMetaGroups();
    for (Map.Entry<String, List<String>> group: metagroups.entrySet()) {
      result.addMetaGroup(group.getKey(), group.getValue());
    }

    return result;
  }

  /**
   * Convenience method to cache the ordering numbers for methods.
   */
  public List<Integer> getInvocationNumbers(String method) {
    if (m_failedInvocationNumbers == null) {
      m_failedInvocationNumbers = Maps.newHashMap();
      for (XmlClass c : getXmlClasses()) {
        for (XmlInclude xi : c.getIncludedMethods()) {
          List<Integer> invocationNumbers = xi.getInvocationNumbers();
          if (invocationNumbers.size() > 0) {
            String methodName = c.getName() + "." + xi.getName();
            m_failedInvocationNumbers.put(methodName, invocationNumbers);
          }
        }
      }
    }

    List<Integer> result = m_failedInvocationNumbers.get(method);
    if (result == null) {
      // Don't use emptyList here since this list might end up receiving values if
      // the test run fails.
      return Lists.newArrayList();
    } else {
      return result;
    }
  }

  public void setPreserveOrder(String preserveOrder) {
    m_preserveOrder = preserveOrder;
  }

  public String getPreserveOrder() {
    String result = m_preserveOrder;
    if (result == null || XmlSuite.DEFAULT_PRESERVE_ORDER.equals(m_preserveOrder)) {
      result = m_suite.getPreserveOrder();
    }

    return result;
  }

  public void setSuite(XmlSuite result) {
    m_suite = result;
  }

  public Boolean getAllowReturnValues() {
    if (m_allowReturnValues != null) return m_allowReturnValues;
    else return getSuite().getAllowReturnValues();
  }

  public void setAllowReturnValues(Boolean allowReturnValues) {
    m_allowReturnValues = allowReturnValues;
  }

  /**
   * Note that this attribute does not come from the XML file, it's calculated
   * internally and represents the order in which this test tag was found in its
   * &lt;suite&gt; tag.  It's used to calculate the ordering of the tests
   * when preserve-test-order is true.
   */
  public int getIndex() {
    return m_index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((m_excludedGroups == null) ? 0 : m_excludedGroups.hashCode());
    result = prime
        * result
        + ((m_failedInvocationNumbers == null) ? 0 : m_failedInvocationNumbers
            .hashCode());
    result = prime * result
        + ((m_includedGroups == null) ? 0 : m_includedGroups.hashCode());
    result = prime * result + ((m_isJUnit == null) ? 0 : m_isJUnit.hashCode());
    result = prime * result
        + ((m_metaGroups == null) ? 0 : m_metaGroups.hashCode());
    result = prime * result
        + ((m_methodSelectors == null) ? 0 : m_methodSelectors.hashCode());
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result
        + ((m_parallel == null) ? 0 : m_parallel.hashCode());
    result = prime * result
        + ((m_parameters == null) ? 0 : m_parameters.hashCode());
    result = prime * result
        + ((m_preserveOrder == null) ? 0 : m_preserveOrder.hashCode());
    result = prime
        * result
        + ((m_skipFailedInvocationCounts == null) ? 0
            : m_skipFailedInvocationCounts.hashCode());
    result = prime * result + m_threadCount;
    result = prime * result + ((m_timeOut == null) ? 0 : m_timeOut.hashCode());
    result = prime * result + ((m_verbose == null) ? 0 : m_verbose.hashCode());
    result = prime * result
        + ((m_xmlClasses == null) ? 0 : m_xmlClasses.hashCode());
    result = prime * result
        + ((m_xmlPackages == null) ? 0 : m_xmlPackages.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null)
      return XmlSuite.f();
    if (getClass() != obj.getClass())
      return XmlSuite.f();
    XmlTest other = (XmlTest) obj;
    if (m_excludedGroups == null) {
      if (other.m_excludedGroups != null)
        return XmlSuite.f();
    } else if (!m_excludedGroups.equals(other.m_excludedGroups))
      return XmlSuite.f();
//    if (m_expression == null) {
//      if (other.m_expression != null)
//        return XmlSuite.f();
//    } else if (!m_expression.equals(other.m_expression))
//      return XmlSuite.f();
    if (m_failedInvocationNumbers == null) {
      if (other.m_failedInvocationNumbers != null)
        return XmlSuite.f();
    } else if (!m_failedInvocationNumbers
        .equals(other.m_failedInvocationNumbers))
      return XmlSuite.f();
    if (m_includedGroups == null) {
      if (other.m_includedGroups != null)
        return XmlSuite.f();
    } else if (!m_includedGroups.equals(other.m_includedGroups))
      return XmlSuite.f();
    if (m_isJUnit == null) {
      if (other.m_isJUnit != null && ! other.m_isJUnit.equals(XmlSuite.DEFAULT_JUNIT))
        return XmlSuite.f();
    } else if (!m_isJUnit.equals(other.m_isJUnit))
      return XmlSuite.f();
    if (m_metaGroups == null) {
      if (other.m_metaGroups != null)
        return XmlSuite.f();
    } else if (!m_metaGroups.equals(other.m_metaGroups))
      return XmlSuite.f();
    if (m_methodSelectors == null) {
      if (other.m_methodSelectors != null)
        return XmlSuite.f();
    } else if (!m_methodSelectors.equals(other.m_methodSelectors))
      return XmlSuite.f();
    if (m_name == null) {
      if (other.m_name != null)
        return XmlSuite.f();
    } else if (!m_name.equals(other.m_name))
      return XmlSuite.f();
    if (m_parallel == null) {
      if (other.m_parallel != null)
        return XmlSuite.f();
    } else if (!m_parallel.equals(other.m_parallel))
      return XmlSuite.f();
    if (m_parameters == null) {
      if (other.m_parameters != null)
        return XmlSuite.f();
    } else if (!m_parameters.equals(other.m_parameters))
      return XmlSuite.f();
    if (m_preserveOrder == null) {
      if (other.m_preserveOrder != null)
        return XmlSuite.f();
    } else if (!m_preserveOrder.equals(other.m_preserveOrder))
      return XmlSuite.f();
    if (m_skipFailedInvocationCounts == null) {
      if (other.m_skipFailedInvocationCounts != null)
        return XmlSuite.f();
    } else if (!m_skipFailedInvocationCounts
        .equals(other.m_skipFailedInvocationCounts))
      return XmlSuite.f();
    if (m_threadCount != other.m_threadCount)
      return XmlSuite.f();
    if (m_timeOut == null) {
      if (other.m_timeOut != null)
        return XmlSuite.f();
    } else if (!m_timeOut.equals(other.m_timeOut))
      return XmlSuite.f();
    if (m_verbose == null) {
      if (other.m_verbose != null)
        return XmlSuite.f();
    } else if (!m_verbose.equals(other.m_verbose))
      return XmlSuite.f();
    if (m_xmlClasses == null) {
      if (other.m_xmlClasses != null)
        return XmlSuite.f();
    } else if (!m_xmlClasses.equals(other.m_xmlClasses))
      return XmlSuite.f();
    if (m_xmlPackages == null) {
      if (other.m_xmlPackages != null)
        return XmlSuite.f();
    } else if (!m_xmlPackages.equals(other.m_xmlPackages))
      return XmlSuite.f();

    return true;
  }

  public void addXmlDependencyGroup(String group, String dependsOn) {
    if (! m_xmlDependencyGroups.containsKey(group)) {
      m_xmlDependencyGroups.put(group, dependsOn);
    } else {
      throw new TestNGException("Duplicate group dependency found for group \"" + group + "\""
          + ", use a space-separated list of groups in the \"depends-on\" attribute");
    }
  }

  public Map<String, String> getXmlDependencyGroups() {
    if (m_xmlGroups != null) {
      Map<String, String> result = Maps.newHashMap();
      List<XmlDependencies> deps = m_xmlGroups.getDependencies();
      for (XmlDependencies d : deps) {
        result.putAll(d.getDependencies());
      }
      return result;
    } else {
      // deprecated
      return m_xmlDependencyGroups;
    }
  }

  @ParentSetter
  public void setXmlSuite(XmlSuite suite) {
	  m_suite = suite;
  }

  private XmlGroups m_xmlGroups;

  public void setGroups(XmlGroups xmlGroups) {
    m_xmlGroups = xmlGroups;
  }
}
