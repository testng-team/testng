package org.testng.xml;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.testng.ITestObjectFactory;
import org.testng.TestNG;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.reporters.XMLStringBuffer;
import org.testng.xml.dom.OnElement;
import org.testng.xml.dom.OnElementList;
import org.testng.xml.dom.Tag;

import static org.testng.collections.CollectionUtils.hasElements;
import static org.testng.internal.Utils.isStringNotEmpty;

/**
 * This class describes the tag &lt;suite&gt; in testng.xml.
 *
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 * @author <a href = 'mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class XmlSuite implements Serializable, Cloneable {
  /** Parallel modes */
  public enum ParallelMode {
    TESTS, METHODS, CLASSES, INSTANCES, NONE, TRUE, FALSE;

    public static XmlSuite.ParallelMode getValidParallel(String parallel) {
      if (parallel == null) {
        return null;
      }
      try {
        return XmlSuite.ParallelMode.valueOf(parallel.toUpperCase());
      } catch (IllegalArgumentException e) {
        return null;
      }
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  /** Configuration failure policy options */
  public static final String SKIP = "skip";
  public static final String CONTINUE = "continue";

  private String m_test;

  /** The default suite name TODO CQ is this OK as a default name. */
  private static final String DEFAULT_SUITE_NAME = "Default Suite";

  /** The suite name (defaults to DEFAULT_SUITE_NAME) */
  private String m_name = DEFAULT_SUITE_NAME;

  /** The suite verbose flag. (0 to 10)*/
  public static final Integer DEFAULT_VERBOSE = 1;
  private Integer m_verbose = null;

  public static final ParallelMode DEFAULT_PARALLEL = ParallelMode.FALSE;
  private ParallelMode m_parallel = DEFAULT_PARALLEL;

  private String m_parentModule = "";
  private String m_guiceStage = "";

  /** Whether to SKIP or CONTINUE to re-attempt failed configuration methods. */
  public static final String DEFAULT_CONFIG_FAILURE_POLICY = SKIP;
  private String m_configFailurePolicy = DEFAULT_CONFIG_FAILURE_POLICY;

  /** JUnit compatibility flag. */
  public static final Boolean DEFAULT_JUNIT = Boolean.FALSE;
  private Boolean m_isJUnit = DEFAULT_JUNIT;

  /** mixed mode flag. */
  public static final Boolean DEFAULT_MIXED = Boolean.FALSE;
  private Boolean m_isMixed = DEFAULT_MIXED;

  public static final Boolean DEFAULT_SKIP_FAILED_INVOCATION_COUNTS = Boolean.FALSE;
  private Boolean m_skipFailedInvocationCounts = DEFAULT_SKIP_FAILED_INVOCATION_COUNTS;

  /** The thread count. */
  public static final Integer DEFAULT_THREAD_COUNT = 5;
  private int m_threadCount = DEFAULT_THREAD_COUNT;

  /** Thread count for the data provider pool */
  public static final Integer DEFAULT_DATA_PROVIDER_THREAD_COUNT = 10;
  private int m_dataProviderThreadCount = DEFAULT_DATA_PROVIDER_THREAD_COUNT;

  /** By default, a method failing will cause all instances of that class to skip */
  public static final Boolean DEFAULT_GROUP_BY_INSTANCES = false;
  private Boolean m_groupByInstances = DEFAULT_GROUP_BY_INSTANCES;

  public static final Boolean DEFAULT_ALLOW_RETURN_VALUES = Boolean.FALSE;
  private Boolean m_allowReturnValues = DEFAULT_ALLOW_RETURN_VALUES;

  /** The packages containing test classes. */
  private List<XmlPackage> m_xmlPackages = Lists.newArrayList();

  /** BeanShell expression. */
  private String m_expression = null;

  /** Suite level method selectors. */
  private List<XmlMethodSelector> m_methodSelectors = Lists.newArrayList();

  /** Tests in suite. */
  private List<XmlTest> m_tests = Lists.newArrayList();

  /** Suite level parameters. */
  private Map<String, String> m_parameters = Maps.newHashMap();

  /** Name of the XML file */
  private String m_fileName;

  /** Time out for methods/tests */
  private String m_timeOut;

  /** List of child XML suite specified using <suite-file> tags */
  private List<XmlSuite> m_childSuites = Lists.newArrayList();

  /** Parent XML Suite if this suite was specified in another suite using <suite-file> tag */
  private XmlSuite m_parentSuite;

  private List<String> m_suiteFiles = Lists.newArrayList();

  private ITestObjectFactory m_objectFactory;

  private List<String> m_listeners = Lists.newArrayList();

  private static final long serialVersionUID = 4999962288272750226L;

  public static final String DEFAULT_PRESERVE_ORDER = "true";
  private String m_preserveOrder = DEFAULT_PRESERVE_ORDER;

  private List<String> m_includedGroups = Lists.newArrayList();
  private List<String> m_excludedGroups = Lists.newArrayList();
  private XmlMethodSelectors m_xmlMethodSelectors;

  /**
   * @return the fileName
   */
  public String getFileName() {
    return m_fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName(String fileName) {
    m_fileName = fileName;
  }

  /**
   * Returns the parallel mode.
   * @return the parallel mode.
   */
  public ParallelMode getParallel() {
    return m_parallel;
  }


  public String getParentModule() {
    return m_parentModule;
  }

  public String getGuiceStage() {
    return m_guiceStage;
  }

  public ITestObjectFactory getObjectFactory() {
    return m_objectFactory;
  }

  public void setObjectFactory(ITestObjectFactory objectFactory) {
    m_objectFactory = objectFactory;
  }

  /**
   * @deprecated Use #setParallel(XmlSuite.ParallelMode) instead
   */
  @Deprecated
  public void setParallel(String parallel) {
    if (parallel == null) {
      setParallel((ParallelMode)null);
    } else {
      setParallel(XmlSuite.ParallelMode.getValidParallel(parallel));
    }
  }

  /**
   * Sets the parallel mode
   * @param parallel the parallel mode
   */
  public void setParallel(ParallelMode parallel) {
    if (parallel == null) {
      m_parallel = DEFAULT_PARALLEL;
    } else {
      m_parallel = parallel;
    }
  }

  public void setParentModule(String parentModule) {
    m_parentModule = parentModule;
  }

  public void setGuiceStage(String guiceStage) {
    m_guiceStage = guiceStage;
  }

  /**
   * Sets the configuration failure policy.
   * @param configFailurePolicy the config failure policy
   */
  public void setConfigFailurePolicy(String configFailurePolicy) {
    m_configFailurePolicy = configFailurePolicy;
  }

  /**
   * Returns the configuration failure policy.
   * @return the configuration failure policy
   */
  public String getConfigFailurePolicy() {
    return m_configFailurePolicy;
  }


  /**
   * Returns the verbose.
   * @return the verbose.
   */
  public Integer getVerbose() {
    return m_verbose != null ? m_verbose : TestNG.DEFAULT_VERBOSE;
  }

  /**
   * Set the verbose.
   * @param verbose The verbose to set.
   */
  public void setVerbose(Integer verbose) {
    m_verbose = verbose;
  }

  /**
   * Returns the name.
   * @return the name.
   */
  public String getName() {
    return m_name;
  }

  /**
   * Sets the name.
   * @param name The name to set.
   */
  public void setName(String name) {
    m_name = name;
  }

  /**
   * Returns the test.
   * @return the test.
   */
  public String getTest() {
    return m_test;
  }

  /**
   * Returns the tests.
   * @return the tests.
   */
  public List<XmlTest> getTests() {
    return m_tests;
  }

  // For YAML
  public void setTests(List<XmlTest> tests) {
    m_tests = tests;
  }

  /**
   * Returns the method selectors.
   *
   * @return the method selectors.
   */
  public List<XmlMethodSelector> getMethodSelectors() {
    if (m_xmlMethodSelectors != null) {
      return m_xmlMethodSelectors.getMethodSelectors();
    } else {
      // deprecated
      return m_methodSelectors;
    }
  }

  /**
   * Sets the method selectors.
   *
   * @param methodSelectors the method selectors.
   */
  public void setMethodSelectors(List<XmlMethodSelector> methodSelectors) {
    m_methodSelectors = Lists.newArrayList(methodSelectors);
  }

  /**
   * Updates the list of parameters that apply to this XML suite. This method
   * should be invoked any time there is a change in the state of this suite that
   * would affect the parameter list.<br>
   * NOTE: Currently being invoked after a parent suite is added or if parameters
   * for this suite are updated.
   */
  private void updateParameters() {
    /*
     * Whatever parameters are set by user or via XML, should be updated
     * using parameters from parent suite, if it exists. Parameters from this
     * suite override the same named parameters from parent suite.
     */
    if (m_parentSuite != null) {
      Set<String> keySet = m_parentSuite.getParameters().keySet();
      for (String name : keySet) {
        if (!m_parameters.containsKey(name)) {
           m_parameters.put(name, m_parentSuite.getParameter(name));
        }
      }
    }
  }

  /**
   * Sets parameters.
   * @param parameters the parameters.
   */
  public void setParameters(Map<String, String> parameters) {
    m_parameters = parameters;
    updateParameters();
  }

  /**
   * Gets the parameters that apply to tests in this suite.<br>
   * Set of parameters for a suite is appended with parameters from parent suite.
   * Also, parameters from this suite override the same named parameters from
   * parent suite.
   */
  public Map<String, String> getParameters() {
    return m_parameters;
  }

  /**
   * @return The parameters defined in this suite and all its XmlTests.
   */
  public Map<String, String> getAllParameters() {
    Map<String, String> result = Maps.newHashMap();
    for (Map.Entry<String, String> entry : m_parameters.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }

    for (XmlTest test : getTests()) {
      Map<String, String> tp = test.getLocalParameters();
      for (Map.Entry<String, String> entry : tp.entrySet()) {
        result.put(entry.getKey(), entry.getValue());
      }
    }

    return result;
  }

  /**
   * Returns the parameter defined in this suite only.
   * @param name the parameter name.
   * @return The parameter defined in this suite only.
   */
  public String getParameter(String name) {
    return m_parameters.get(name);
  }

  /**
   * @return The threadCount.
   */
  public int getThreadCount() {
    return m_threadCount;
  }

  /**
   * Set the thread count.
   * @param threadCount The thread count to set.
   */
  public void setThreadCount(int threadCount) {
    m_threadCount = threadCount;
  }

  /**
   * @return The JUnit compatibility flag.
   */
  public Boolean isJUnit() {
    return m_isJUnit;
  }

  /**
   * Sets the JUnit compatibility flag.
   *
   * @param isJUnit the JUnit compatibility flag.
   */
  public void setJUnit(Boolean isJUnit) {
    m_isJUnit = isJUnit;
  }

  // For YAML
  public void setJunit(Boolean j) {
    setJUnit(j);
  }

  public Boolean skipFailedInvocationCounts() {
    return m_skipFailedInvocationCounts;
  }

  public void setSkipFailedInvocationCounts(boolean skip) {
    m_skipFailedInvocationCounts = skip;
  }

  /**
   * Sets the XML packages.
   *
   * @param packages the XML packages.
   */
  public void setXmlPackages(List<XmlPackage> packages) {
    m_xmlPackages = Lists.newArrayList(packages);
  }

  /**
   * Returns the XML packages.
   *
   * @return the XML packages.
   */
  public List<XmlPackage> getXmlPackages() {
    return m_xmlPackages;
  }


  // For YAML
  public List<XmlPackage> getPackages() {
    return getXmlPackages();
  }

  @Tag(name = "method-selectors")
  public void setMethodSelectors(XmlMethodSelectors xms) {
    m_xmlMethodSelectors = xms;
  }

  // For YAML
  public void setPackages(List<XmlPackage> packages) {
    setXmlPackages(packages);
  }

  /**
   * @return A String representation of this XML suite.
   */
  public String toXml() {
    XMLStringBuffer xsb = new XMLStringBuffer();
    xsb.setDocType("suite SYSTEM \"" + Parser.TESTNG_DTD_URL + '\"');
    Properties p = new Properties();
    p.setProperty("name", getName());
    if (getVerbose() != null) {
      XmlUtils.setProperty(p, "verbose", getVerbose().toString(), DEFAULT_VERBOSE.toString());
    }
    final ParallelMode parallel= getParallel();
    if(parallel != null && !DEFAULT_PARALLEL.equals(parallel)) {
      p.setProperty("parallel", parallel.toString());
    }
    XmlUtils.setProperty(p, "group-by-instances", String.valueOf(getGroupByInstances()),
        DEFAULT_GROUP_BY_INSTANCES.toString());
    XmlUtils.setProperty(p, "configfailurepolicy", getConfigFailurePolicy(),
        DEFAULT_CONFIG_FAILURE_POLICY);
    XmlUtils.setProperty(p, "thread-count", String.valueOf(getThreadCount()),
        DEFAULT_THREAD_COUNT.toString());
    XmlUtils.setProperty(p, "data-provider-thread-count", String.valueOf(getDataProviderThreadCount()),
        DEFAULT_DATA_PROVIDER_THREAD_COUNT.toString());
    if (! DEFAULT_JUNIT.equals(m_isJUnit)) {
      p.setProperty("junit", m_isJUnit != null ? m_isJUnit.toString() : "false"); // TESTNG-141
    }
    XmlUtils.setProperty(p, "skipfailedinvocationcounts", m_skipFailedInvocationCounts.toString(),
        DEFAULT_SKIP_FAILED_INVOCATION_COUNTS.toString());
    if(null != m_objectFactory) {
      p.setProperty("object-factory", m_objectFactory.getClass().getName());
    }
    if (isStringNotEmpty(m_parentModule)) {
      p.setProperty("parent-module", getParentModule());
    }
    if (isStringNotEmpty(m_guiceStage)) {
      p.setProperty("guice-stage", getGuiceStage());
    }
    XmlUtils.setProperty(p, "allow-return-values", String.valueOf(getAllowReturnValues()),
        DEFAULT_ALLOW_RETURN_VALUES.toString());
    xsb.push("suite", p);

    XmlUtils.dumpParameters(xsb, m_parameters);

    if (hasElements(m_listeners)) {
      xsb.push("listeners");
      for (String listenerName: m_listeners) {
        Properties listenerProps = new Properties();
        listenerProps.setProperty("class-name", listenerName);
        xsb.addEmptyElement("listener", listenerProps);
      }
      xsb.pop("listeners");
    }

    if (hasElements(getXmlPackages())) {
      xsb.push("packages");

      for (XmlPackage pack : getXmlPackages()) {
        xsb.getStringBuffer().append(pack.toXml("    "));
      }

      xsb.pop("packages");
    }

    if (getXmlMethodSelectors() != null) {
      xsb.getStringBuffer().append(getXmlMethodSelectors().toXml("  "));
    } else {
      // deprecated
      if (hasElements(getMethodSelectors())) {
        xsb.push("method-selectors");
        for (XmlMethodSelector selector : getMethodSelectors()) {
          xsb.getStringBuffer().append(selector.toXml("  "));
        }

        xsb.pop("method-selectors");
      }
    }

    List<String> suiteFiles = getSuiteFiles();
    if (suiteFiles.size() > 0) {
      xsb.push("suite-files");
      for (String sf : suiteFiles) {
        Properties prop = new Properties();
        prop.setProperty("path", sf);
        xsb.addEmptyElement("suite-file", prop);
      }
      xsb.pop("suite-files");
    }

    List<String> included = getIncludedGroups();
    List<String> excluded = getExcludedGroups();
    if (hasElements(included) || hasElements(excluded)) {
      xsb.push("groups");
      xsb.push("run");
      for (String g : included) {
        xsb.addEmptyElement("include", "name", g);
      }
      for (String g : excluded) {
        xsb.addEmptyElement("exclude", "name", g);
      }
      xsb.pop("run");
      xsb.pop("groups");
    }

    if (m_xmlGroups != null) {
      xsb.getStringBuffer().append(m_xmlGroups.toXml("  "));
    }

    for (XmlTest test : getTests()) {
      xsb.getStringBuffer().append(test.toXml("  "));
    }

    xsb.pop("suite");

    return xsb.toXML();
  }

  @Tag(name = "method-selectors")
  public void setXmlMethodSelectors(XmlMethodSelectors xms) {
    m_xmlMethodSelectors = xms;
  }

  private XmlMethodSelectors getXmlMethodSelectors() {
    return m_xmlMethodSelectors;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("[Suite: \"").append( m_name).append( "\" ");

    for (XmlTest t : m_tests) {
      result.append("  ").append( t.toString()).append(' ');
    }

    for (XmlMethodSelector ms : m_methodSelectors) {
      result.append(" methodSelector:" + ms);
    }

    result.append(']');

    return result.toString();
  }

  /**
   * Logs to System.out.
   * @param s the message to log.
   */
  private static void ppp(String s) {
    System.out.println("[XmlSuite] " + s);
  }

  /**
   * {@inheritDoc}
   * Note that this is not a full clone:  XmlTest children are not cloned by this
   * method.
   */
  @Override
  public Object clone() {
    XmlSuite result = new XmlSuite();
    
    result.setExcludedGroups(getExcludedGroups());
    result.setIncludedGroups(getIncludedGroups());
    result.setGroupByInstances(getGroupByInstances());
    result.setGroups(getGroups());
    result.setMethodSelectors(getXmlMethodSelectors());
    result.setPackages(getPackages());
    result.setParentSuite(getParentSuite());
    result.setPreserveOrder(getPreserveOrder());
    result.setSuiteFiles(getSuiteFiles());
    result.setTests(getTests());
    result.setXmlMethodSelectors(getXmlMethodSelectors());
    result.setName(getName());
    result.setFileName(getFileName());
    result.setListeners(getListeners());
    result.setParallel(getParallel());
    result.setParentModule(getParentModule());
    result.setGuiceStage(getGuiceStage());
    result.setConfigFailurePolicy(getConfigFailurePolicy());
    result.setThreadCount(getThreadCount());
    result.setDataProviderThreadCount(getDataProviderThreadCount());
    result.setParameters(getAllParameters());
    result.setVerbose(getVerbose());
    result.setXmlPackages(getXmlPackages());
//    result.setBeanShellExpression(getExpression());
    result.setMethodSelectors(getMethodSelectors());
    result.setJUnit(isJUnit()); // TESTNG-141
    result.setSkipFailedInvocationCounts(skipFailedInvocationCounts());
    result.setObjectFactory(getObjectFactory());
    result.setAllowReturnValues(getAllowReturnValues());
    result.setTimeOut(getTimeOut());
    return result;
  }

  /**
   * Sets the timeout.
   *
   * @param timeOut the timeout.
   */
  public void setTimeOut(String timeOut) {
    m_timeOut = timeOut;
  }

  /**
   * Returns the timeout.
   * @return the timeout.
   */
  public String getTimeOut() {
    return m_timeOut;
  }
  
  /**
   * Returns the timeout as a long value specifying the default value to be used if
   * no timeout was specified.
   *
   * @param def the the default value to be used if no timeout was specified.
   * @return the timeout as a long value specifying the default value to be used if
   * no timeout was specified.
   */
  public long getTimeOut(long def) {
    long result = def;
    if (m_timeOut != null) {
        result = Long.parseLong(m_timeOut);
    }
    
    return result;
  }

  /**
   * Sets the suite files.
   *
   * @param files the suite files.
   */
  public void setSuiteFiles(List<String> files) {
    m_suiteFiles = files;
  }
  
  /**
   * Returns the suite files.
   * @return the suite files.
   */
  public List<String> getSuiteFiles() {
    return m_suiteFiles;
  }

  public void setListeners(List<String> listeners) {
    m_listeners = listeners;
  }

  public List<String> getListeners() {
    if (m_parentSuite != null) {
      List<String> listeners = m_parentSuite.getListeners();
      for (String listener : listeners) {
        if (!m_listeners.contains(listener)) {
           m_listeners.add(listener);
        }
      }
    }
    return m_listeners;
  }

  public void setDataProviderThreadCount(int count) {
    m_dataProviderThreadCount = count;
  }

  public int getDataProviderThreadCount() {
    // org.testng.CommandLineArgs.DATA_PROVIDER_THREAD_COUNT
    String s = System.getProperty("dataproviderthreadcount");
    if (s != null) {
      try {
        int nthreads = Integer.parseInt(s);
        return nthreads;
      } catch(NumberFormatException nfe) {
        System.err.println("Parsing System property 'dataproviderthreadcount': " + nfe);
      }
    }
    return m_dataProviderThreadCount;
  }

  public void setParentSuite(XmlSuite parentSuite) {
    m_parentSuite = parentSuite;
    updateParameters();
  }

  public XmlSuite getParentSuite() {
    return m_parentSuite;
  }

  public List<XmlSuite> getChildSuites() {
    return m_childSuites;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
//      result = prime * result
//          + ((m_childSuites == null) ? 0 : m_childSuites.hashCode());
    result = prime
        * result
        + ((m_configFailurePolicy == null) ? 0 : m_configFailurePolicy
            .hashCode());
    result = prime * result + m_dataProviderThreadCount;
    result = prime * result
        + ((m_expression == null) ? 0 : m_expression.hashCode());
    result = prime * result
        + ((m_fileName == null) ? 0 : m_fileName.hashCode());
    result = prime * result
        + ((m_isJUnit == null) ? 0 : m_isJUnit.hashCode());
    result = prime * result
        + ((m_listeners == null) ? 0 : m_listeners.hashCode());

    result = prime * result
        + ((m_methodSelectors == null) ? 0 : m_methodSelectors.hashCode());
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    result = prime * result
        + ((m_objectFactory == null) ? 0 : m_objectFactory.hashCode());
    result = prime * result
        + ((m_parallel == null) ? 0 : m_parallel.hashCode());
//    result = prime * result
//        + ((m_parameters == null) ? 0 : m_parameters.hashCode());
//      result = prime * result
//          + ((m_parentSuite == null) ? 0 : m_parentSuite.hashCode());
    result = prime
        * result
        + ((m_skipFailedInvocationCounts == null) ? 0
            : m_skipFailedInvocationCounts.hashCode());
    result = prime * result
        + ((m_suiteFiles == null) ? 0 : m_suiteFiles.hashCode());
    result = prime * result + ((m_test == null) ? 0 : m_test.hashCode());
    result = prime * result + ((m_tests == null) ? 0 : m_tests.hashCode());
    result = prime * result + m_threadCount;
    result = prime * result
        + ((m_timeOut == null) ? 0 : m_timeOut.hashCode());
    result = prime * result
        + ((m_verbose == null) ? 0 : m_verbose.hashCode());
    result = prime * result
        + ((m_xmlPackages == null) ? 0 : m_xmlPackages.hashCode());
    return result;
  }

  /**
   * Used to debug equals() bugs.
   */
  static boolean f() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return f();
    }
    if (getClass() != obj.getClass()) {
      return f();
    }
    XmlSuite other = (XmlSuite) obj;
//      if (m_childSuites == null) {
//        if (other.m_childSuites != null)
//          return f();
//      } else if (!m_childSuites.equals(other.m_childSuites))
//        return f();
    if (m_configFailurePolicy == null) {
      if (other.m_configFailurePolicy != null) {
        return f();
      }
    } else if (!m_configFailurePolicy.equals(other.m_configFailurePolicy)) {
      return f();
    }
    if (m_dataProviderThreadCount != other.m_dataProviderThreadCount) {
      return f();
    }
    if (m_expression == null) {
      if (other.m_expression != null) {
        return f();
      }
    } else if (!m_expression.equals(other.m_expression)) {
      return f();
    }
    if (m_isJUnit == null) {
      if (other.m_isJUnit != null) {
        return f();
      }
    } else if (!m_isJUnit.equals(other.m_isJUnit)) {
      return f();
    }
    if (m_listeners == null) {
      if (other.m_listeners != null) {
        return f();
      }
    } else if (!m_listeners.equals(other.m_listeners)) {
      return f();
    }
    if (m_methodSelectors == null) {
      if (other.m_methodSelectors != null) {
        return f();
      }
    } else if (!m_methodSelectors.equals(other.m_methodSelectors)) {
      return f();
    }
    if (m_name == null) {
      if (other.m_name != null) {
        return f();
      }
    } else if (!m_name.equals(other.m_name)) {
      return f();
    }
    if (m_objectFactory == null) {
      if (other.m_objectFactory != null) {
        return f();
      }
    } else if (!m_objectFactory.equals(other.m_objectFactory)) {
      return f();
    }
    if (m_parallel == null) {
      if (other.m_parallel != null) {
        return f();
      }
    } else if (!m_parallel.equals(other.m_parallel)) {
      return f();
    }
//    if (m_parameters == null) {
//      if (other.m_parameters != null) {
//        return f();
//      }
//    } else if (!m_parameters.equals(other.m_parameters)) {
//      return f();
//    }
//      if (m_parentSuite == null) {
//        if (other.m_parentSuite != null)
//          return f();
//      } else if (!m_parentSuite.equals(other.m_parentSuite))
//        return f();
    if (m_skipFailedInvocationCounts == null) {
      if (other.m_skipFailedInvocationCounts != null)
        return f();
    } else if (!m_skipFailedInvocationCounts
        .equals(other.m_skipFailedInvocationCounts))
      return f();
    if (m_suiteFiles == null) {
      if (other.m_suiteFiles != null)
        return f();
    } else if (!m_suiteFiles.equals(other.m_suiteFiles))
      return f();
    if (m_test == null) {
      if (other.m_test != null)
        return f();
    } else if (!m_test.equals(other.m_test))
      return f();
    if (m_tests == null) {
      if (other.m_tests != null)
        return f();
    } else if (!m_tests.equals(other.m_tests))
      return f();
    if (m_threadCount != other.m_threadCount)
      return f();
    if (m_timeOut == null) {
      if (other.m_timeOut != null)
        return f();
    } else if (!m_timeOut.equals(other.m_timeOut))
      return f();
    if (m_verbose == null) {
      if (other.m_verbose != null)
        return f();
    } else if (!m_verbose.equals(other.m_verbose))
      return f();
    if (m_xmlPackages == null) {
      if (other.m_xmlPackages != null)
        return f();
    } else if (!m_xmlPackages.equals(other.m_xmlPackages))
      return f();
    return true;
  }


  /**
   * The DTD sometimes forces certain attributes to receive a default value. Such
   * a value is considered equal to another one if that other one is null.
   */
  private boolean eq(String o1, String o2, String def) {
    boolean result = false;
    if (o1 == null && o2 == null) result = true;
    else if (o1 != null) {
      result = o1.equals(o2) || (def.equals(o1) && o2 == null);
    }
    else if (o2 != null) {
      result = o2.equals(o1) || (def.equals(o2) && o1 == null);
    }
    return result;
  }

  public void setPreserveOrder(String f) {
    m_preserveOrder = f;
  }

  public String getPreserveOrder() {
    return m_preserveOrder;
  }

  /**
   * @return Returns the includedGroups.
   * Note: do not modify the returned value, use {@link #addIncludedGroup(String)}.
   */
  public List<String> getIncludedGroups() {
    if (m_xmlGroups != null) {
      return m_xmlGroups.getRun().getIncludes();
    } else {
      // deprecated
      return m_includedGroups;
    }
  }

  public void addIncludedGroup(String g) {
    m_includedGroups.add(g);
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
    if (m_xmlGroups != null) {
      return m_xmlGroups.getRun().getExcludes();
    } else {
      return m_excludedGroups;
    }
  }

  public void addExcludedGroup(String g) {
    m_excludedGroups.add(g);
  }

  public Boolean getGroupByInstances() {
    return m_groupByInstances;
  }

  public void setGroupByInstances(boolean f) {
    m_groupByInstances = f;
  }

  public void addListener(String listener) {
    m_listeners.add(listener);
  }

  public Boolean getAllowReturnValues() {
    return m_allowReturnValues;
  }

  public void setAllowReturnValues(Boolean allowReturnValues) {
    m_allowReturnValues = allowReturnValues;
  }

  private XmlGroups m_xmlGroups;

  public void setGroups(XmlGroups xmlGroups) {
    m_xmlGroups = xmlGroups;
  }

  @OnElement(tag = "parameter", attributes = { "name", "value" })
  public void onParameterElement(String name, String value) {
    getParameters().put(name, value);
  }

  @OnElementList(tag = "listeners", attributes = { "class-name" })
  public void onListenerElement(String className) {
    addListener(className);
  }

  @OnElementList(tag = "suite-files", attributes = { "path" })
  public void onSuiteFilesElement(String path) {
    getSuiteFiles().add(path);
  }

  @OnElementList(tag = "packages", attributes = { "name" })
  public void onPackagesElement(String name) {
    getPackages().add(new XmlPackage(name));
  }

//  @OnElementList(tag = "method-selectors", attributes = { "language", "name", "priority" })
  public void onMethodSelectorElement(String language, String name, String priority) {
    System.out.println("Language:" + language);
  }

  public XmlGroups getGroups() {
    return m_xmlGroups;
  }

  public void addTest(XmlTest test) {
    getTests().add(test);
  }

  public Collection<String> getPackageNames() {
    List<String> result = Lists.newArrayList();
    for (XmlPackage p : getPackages()) {
      result.add(p.getName());
    }
    return result;
  }
}

