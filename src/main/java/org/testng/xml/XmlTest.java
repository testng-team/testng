package org.testng.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.xml.dom.ParentSetter;

import static org.testng.xml.XmlSuite.ParallelMode.skipDeprecatedValues;

/** This class describes the tag &lt;test&gt; in testng.xml. */
public class XmlTest implements Cloneable {

  public static final int DEFAULT_TIMEOUT_MS = Integer.MAX_VALUE;

  private XmlSuite m_suite;
  private String m_name;
  private Integer m_verbose = XmlSuite.DEFAULT_VERBOSE;
  private Boolean m_isJUnit = XmlSuite.DEFAULT_JUNIT;
  private int m_threadCount = -1;

  private List<XmlClass> m_xmlClasses = Lists.newArrayList();

  private Map<String, String> m_parameters = Maps.newHashMap();
  private XmlSuite.ParallelMode m_parallel;

  private List<XmlMethodSelector> m_methodSelectors = Lists.newArrayList();
  // test level packages
  private List<XmlPackage> m_xmlPackages = Lists.newArrayList();

  private String m_timeOut;
  private Boolean m_skipFailedInvocationCounts = XmlSuite.DEFAULT_SKIP_FAILED_INVOCATION_COUNTS;
  private Map<String, List<Integer>> m_failedInvocationNumbers = null; // lazily initialized

  private Boolean m_preserveOrder = XmlSuite.DEFAULT_PRESERVE_ORDER;

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
    // no two tests in the same suite should have the same name.
    // so, make the default test name unique
    m_name = TestNG.DEFAULT_COMMAND_LINE_TEST_NAME + " " + UUID.randomUUID().toString();
  }

  // For YAML
  public XmlTest() {}

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
   *
   * @return the suite this test is part of.
   */
  public final XmlSuite getSuite() {
    if (m_suite == null) {
      throw new IllegalStateException(
          "Current [XmlTest] object is not associated with any [XmlSuite] yet.");
    }
    return m_suite;
  }

  /** @return the includedGroups. */
  public List<String> getIncludedGroups() {
    List<String> result = Lists.newArrayList();
    if (m_xmlGroups != null && m_xmlGroups.getRun() != null) {
      result.addAll(m_xmlGroups.getRun().getIncludes());
    }
    result.addAll(getSuite().getIncludedGroups());
    return Collections.unmodifiableList(result);
  }

  /**
   * Sets the XML Classes.
   *
   * @param classes The classes to set.
   * @deprecated use setXmlClasses
   */
  @Deprecated
  public void setClassNames(List<XmlClass> classes) {
    m_xmlClasses = classes;
  }

  /** @return Returns the classes. */
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
   *
   * @param classes The classes to set.
   */
  public void setXmlClasses(List<XmlClass> classes) {
    m_xmlClasses = classes;
  }

  /** @return Returns the name. */
  public String getName() {
    return m_name;
  }

  /** @param name The name to set. */
  public void setName(String name) {
    m_name = name;
  }

  /** @param v - Verbosity level. */
  public void setVerbose(int v) {
    m_verbose = v;
  }

  public int getThreadCount() {
    return m_threadCount > 0 ? m_threadCount : getSuite().getThreadCount();
  }

  public void setThreadCount(int threadCount) {
    m_threadCount = threadCount;
  }

  public void setIncludedGroups(List<String> g) {
    if (m_xmlGroups == null) {
      m_xmlGroups = new XmlGroups();
    }
    if (m_xmlGroups.getRun() == null) {
      m_xmlGroups.setRun(new XmlRun());
    }
    List<String> includes = m_xmlGroups.getRun().getIncludes();
    includes.clear();
    includes.addAll(g);
  }

  public void setExcludedGroups(List<String> g) {
    if (m_xmlGroups == null) {
      m_xmlGroups = new XmlGroups();
    }
    if (m_xmlGroups.getRun() == null) {
      m_xmlGroups.setRun(new XmlRun());
    }
    List<String> excludes = m_xmlGroups.getRun().getExcludes();
    excludes.clear();
    excludes.addAll(g);
  }

  public List<String> getExcludedGroups() {
    List<String> result = new ArrayList<>();
    if (m_xmlGroups != null && m_xmlGroups.getRun() != null) {
      result.addAll(m_xmlGroups.getRun().getExcludes());
    }
    result.addAll(getSuite().getExcludedGroups());
    return Collections.unmodifiableList(result);
  }

  public void addIncludedGroup(String g) {
    if (m_xmlGroups == null) {
      m_xmlGroups = new XmlGroups();
      m_xmlGroups.setRun(new XmlRun());
    }
    m_xmlGroups.getRun().getIncludes().add(g);
  }

  public void addExcludedGroup(String g) {
    if (m_xmlGroups == null) {
      m_xmlGroups = new XmlGroups();
    }
    if (m_xmlGroups.getRun() == null) {
      m_xmlGroups.setRun(new XmlRun());
    }
    m_xmlGroups.getRun().getExcludes().add(g);
  }

  /** @return Returns the verbose. */
  public int getVerbose() {
    Integer result = m_verbose;
    if (null == result || XmlSuite.DEFAULT_VERBOSE.equals(m_verbose)) {
      result = getSuite().getVerbose();
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
      result = getSuite().getGroupByInstances();
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

  /** @return Returns the isJUnit. */
  public boolean isJUnit() {
    Boolean result = m_isJUnit;
    if (null == result || XmlSuite.DEFAULT_JUNIT.equals(result)) {
      result = getSuite().isJUnit();
    }

    return result;
  }

  /** @param isJUnit The isJUnit to set. */
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

  /** @return Returns the isJUnit. */
  public boolean skipFailedInvocationCounts() {
    Boolean result = m_skipFailedInvocationCounts;
    if (null == result) {
      result = getSuite().skipFailedInvocationCounts();
    }

    return result;
  }

  public void addMetaGroup(String name, List<String> metaGroup) {
    if (m_xmlGroups == null) {
      m_xmlGroups = new XmlGroups();
    }
    XmlDefine define = new XmlDefine();
    define.setName(name);
    define.getIncludes().addAll(metaGroup);
    m_xmlGroups.getDefines().add(define);
  }

  public void addMetaGroup(String name, String... metaGroup) {
    addMetaGroup(name, Arrays.asList(metaGroup));
  }

  // For YAML
  public void setMetaGroups(Map<String, List<String>> metaGroups) {
    for (Map.Entry<String, List<String>> entry : metaGroups.entrySet()) {
      addMetaGroup(entry.getKey(), entry.getValue());
    }
  }

  /** @return Returns the metaGroups. */
  public Map<String, List<String>> getMetaGroups() {
    if (m_xmlGroups == null) {
      return Collections.emptyMap();
    }
    Map<String, List<String>> result = Maps.newHashMap();
    List<XmlDefine> defines = m_xmlGroups.getDefines();
    for (XmlDefine xd : defines) {
      result.put(xd.getName(), xd.getIncludes());
    }
    return result;
  }

  /** @param parameters - A {@link Map} of parameters. */
  public void setParameters(Map<String, String> parameters) {
    m_parameters = parameters;
  }

  public void addParameter(String key, String value) {
    m_parameters.put(key, value);
  }

  public String getParameter(String name) {
    String result = m_parameters.get(name);
    if (null == result) {
      result = getSuite().getParameter(name);
    }

    return result;
  }

  /** @return the parameters defined in this test tag and the tags above it. */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = Maps.newHashMap();
    result.putAll(getSuite().getParameters());
    result.putAll(m_parameters);
    return result;
  }

  /**
   * @return the parameters defined in this tag, and only this test tag. To retrieve the inherited
   *     parameters as well, call {@code getAllParameters()}.
   */
  public Map<String, String> getLocalParameters() {
    return m_parameters;
  }

  /** @deprecated Use {@code getLocalParameters()} or {@code getAllParameters()} */
  @Deprecated
  public Map<String, String> getParameters() {
    return getAllParameters();
  }

  /**
   * @deprecated Use {@code getLocalParameters()} instead
   * @return the parameters defined on this <test> tag only
   */
  @Deprecated
  public Map<String, String> getTestParameters() {
    return getLocalParameters();
  }

  public void setParallel(XmlSuite.ParallelMode parallel) {
    m_parallel = skipDeprecatedValues(parallel);
  }

  public XmlSuite.ParallelMode getParallel() {
    XmlSuite.ParallelMode result = getSuite().getParallel();
    if (null != m_parallel) {
      result = m_parallel;
    }

    return result;
  }

  public String getTimeOut() {
    String result = getSuite().getTimeOut();
    if (null != m_timeOut) {
      result = m_timeOut;
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

  /**
   * @deprecated Use {@link #setScript(XmlScript)} instead.
   */
  @Deprecated
  public void setExpression(String expression) {
    setBeanShellExpression(expression);
  }

  /**
   * @deprecated Use {@link #setScript(XmlScript)} instead.
   */
  @Deprecated
  public void setBeanShellExpression(String expression) {
    if (expression != null) {
      XmlScript script = new XmlScript();
      script.setExpression(expression);
      script.setLanguage("BeanShell");
    }
  }

  public void setScript(XmlScript script) {
    List<XmlMethodSelector> selectors = getMethodSelectors();
    if (selectors.size() > 0) {
      XmlMethodSelector xms = selectors.get(0);
      xms.setScript(script);
    } else if (script != null) {
      XmlMethodSelector xms = new XmlMethodSelector();
      selectors.add(xms);
      xms.setScript(script);
    }
  }

  /**
   * @deprecated Use {@link #getScript()} instead.
   */
  @Deprecated
  public String getExpression() {
    XmlScript script = getScript();
    if (script == null) {
      return null;
    }
    return script.getExpression();
  }

  public XmlScript getScript() {
    List<XmlMethodSelector> selectors = getMethodSelectors();
    if (selectors.isEmpty()) {
      return null;
    }
    return selectors.get(0).getScript();
  }

  public String toXml(String indent) {
    return XmlWeaver.asXml(this, indent);
  }

  /**
   * Clone the <TT>source</TT> <CODE>XmlTest</CODE> by including: - test attributes - groups
   * definitions - parameters
   *
   * <p>The &lt;classes&gt; sub element is ignored for the moment.
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
    for (Map.Entry<String, List<String>> group : metagroups.entrySet()) {
      result.addMetaGroup(group.getKey(), group.getValue());
    }

    return result;
  }

  /** Convenience method to cache the ordering numbers for methods. */
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

  /** @deprecated Use {@link #setPreserveOrder(Boolean)} instead */
  @Deprecated
  public void setPreserveOrder(String preserveOrder) {
    setPreserveOrder(Boolean.valueOf(preserveOrder));
  }

  public void setPreserveOrder(Boolean preserveOrder) {
    m_preserveOrder = preserveOrder;
  }

  public Boolean getPreserveOrder() {
    if (m_preserveOrder == null) {
      return getSuite().getPreserveOrder();
    }

    return m_preserveOrder;
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
   * Note that this attribute does not come from the XML file, it's calculated internally and
   * represents the order in which this test tag was found in its &lt;suite&gt; tag. It's used to
   * calculate the ordering of the tests when preserve-test-order is true.
   */
  public int getIndex() {
    return m_index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((m_xmlGroups == null || m_xmlGroups.getRun() == null)
                ? 0
                : m_xmlGroups.getRun().getExcludes().hashCode());
    result =
        prime * result
            + ((m_failedInvocationNumbers == null) ? 0 : m_failedInvocationNumbers.hashCode());
    result =
        prime * result
            + ((m_xmlGroups == null || m_xmlGroups.getRun() == null)
                ? 0
                : m_xmlGroups.getRun().getIncludes().hashCode());
    result = prime * result + ((m_isJUnit == null) ? 0 : m_isJUnit.hashCode());
    result = prime * result + ((m_xmlGroups == null) ? 0 : m_xmlGroups.getDefines().hashCode());
    result = prime * result + ((m_methodSelectors == null) ? 0 : m_methodSelectors.hashCode());
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result + ((m_parallel == null) ? 0 : m_parallel.hashCode());
    result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
    result = prime * result + ((m_preserveOrder == null) ? 0 : m_preserveOrder.hashCode());
    result =
        prime * result
            + ((m_skipFailedInvocationCounts == null)
                ? 0
                : m_skipFailedInvocationCounts.hashCode());
    result = prime * result + m_threadCount;
    result = prime * result + ((m_timeOut == null) ? 0 : m_timeOut.hashCode());
    result = prime * result + ((m_verbose == null) ? 0 : m_verbose.hashCode());
    result = prime * result + ((m_xmlClasses == null) ? 0 : m_xmlClasses.hashCode());
    result = prime * result + ((m_xmlPackages == null) ? 0 : m_xmlPackages.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) return XmlSuite.f();
    if (getClass() != obj.getClass()) return XmlSuite.f();
    XmlTest other = (XmlTest) obj;
    if (m_xmlGroups == null) {
      if (other.m_xmlGroups != null) return XmlSuite.f();
    } else {
      if (other.m_xmlGroups == null) {
        return false;
      }
      if ((m_xmlGroups.getRun() == null && other.m_xmlGroups != null)
          || m_xmlGroups.getRun() != null && other.m_xmlGroups == null) {
        return false;
      }
      if (!m_xmlGroups.getRun().getExcludes().equals(other.m_xmlGroups.getRun().getExcludes())) {
        return XmlSuite.f();
      }
      if (!m_xmlGroups.getRun().getIncludes().equals(other.m_xmlGroups.getRun().getIncludes())) {
        return XmlSuite.f();
      }
      if (!m_xmlGroups.getDefines().equals(other.m_xmlGroups.getDefines())) {
        return false;
      }
    }
    if (m_failedInvocationNumbers == null) {
      if (other.m_failedInvocationNumbers != null) return XmlSuite.f();
    } else if (!m_failedInvocationNumbers.equals(other.m_failedInvocationNumbers))
      return XmlSuite.f();
    if (m_isJUnit == null) {
      if (other.m_isJUnit != null && !other.m_isJUnit.equals(XmlSuite.DEFAULT_JUNIT))
        return XmlSuite.f();
    } else if (!m_isJUnit.equals(other.m_isJUnit)) return XmlSuite.f();
    if (m_methodSelectors == null) {
      if (other.m_methodSelectors != null) return XmlSuite.f();
    } else if (!m_methodSelectors.equals(other.m_methodSelectors)) return XmlSuite.f();
    if (m_name == null) {
      if (other.m_name != null) return XmlSuite.f();
    } else if (!m_name.equals(other.m_name)) return XmlSuite.f();
    if (m_parallel == null) {
      if (other.m_parallel != null) return XmlSuite.f();
    } else if (!m_parallel.equals(other.m_parallel)) return XmlSuite.f();
    if (m_parameters == null) {
      if (other.m_parameters != null) return XmlSuite.f();
    } else if (!m_parameters.equals(other.m_parameters)) return XmlSuite.f();
    if (m_preserveOrder == null) {
      if (other.m_preserveOrder != null) return XmlSuite.f();
    } else if (!m_preserveOrder.equals(other.m_preserveOrder)) return XmlSuite.f();
    if (m_skipFailedInvocationCounts == null) {
      if (other.m_skipFailedInvocationCounts != null) return XmlSuite.f();
    } else if (!m_skipFailedInvocationCounts.equals(other.m_skipFailedInvocationCounts))
      return XmlSuite.f();
    if (m_threadCount != other.m_threadCount) return XmlSuite.f();
    if (m_timeOut == null) {
      if (other.m_timeOut != null) return XmlSuite.f();
    } else if (!m_timeOut.equals(other.m_timeOut)) return XmlSuite.f();
    if (m_verbose == null) {
      if (other.m_verbose != null) return XmlSuite.f();
    } else if (!m_verbose.equals(other.m_verbose)) return XmlSuite.f();
    if (m_xmlClasses == null) {
      if (other.m_xmlClasses != null) return XmlSuite.f();
    } else if (!m_xmlClasses.equals(other.m_xmlClasses)) return XmlSuite.f();
    if (m_xmlPackages == null) {
      if (other.m_xmlPackages != null) return XmlSuite.f();
    } else if (!m_xmlPackages.equals(other.m_xmlPackages)) return XmlSuite.f();

    return true;
  }

  public void addXmlDependencyGroup(String group, String dependsOn) {
    if (!m_xmlDependencyGroups.containsKey(group)) {
      m_xmlDependencyGroups.put(group, dependsOn);
    } else {
      throw new TestNGException(
          "Duplicate group dependency found for group \""
              + group
              + "\""
              + ", use a space-separated list of groups in the \"depends-on\" attribute");
    }
  }

  public Map<String, String> getXmlDependencyGroups() {
    Map<String, String> result = m_xmlDependencyGroups;
    if (m_xmlGroups != null) {
      List<XmlDependencies> deps = m_xmlGroups.getDependencies();
      for (XmlDependencies d : deps) {
        result.putAll(d.getDependencies());
      }
    }
    return result;
  }

  @ParentSetter
  public void setXmlSuite(XmlSuite suite) {
    m_suite = suite;
  }

  private XmlGroups m_xmlGroups;

  public void setGroups(XmlGroups xmlGroups) {
    m_xmlGroups = xmlGroups;
  }

  public XmlGroups getXmlGroups() {
    return m_xmlGroups;
  }

  /**
   * @param names The list of names to check.
   * @return <code>true</code> if the current test's name matches with any of the given names.
   */
  public boolean nameMatchesAny(List<String> names) {
    return names.contains(getName());
  }
}
