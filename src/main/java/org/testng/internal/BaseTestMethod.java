package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

/**
 * Superclass to represent both &#64;Test and &#64;Configuration methods.
 */
public abstract class BaseTestMethod implements ITestNGMethod {
  private static final long serialVersionUID = -2666032602580652173L;
  private static final Pattern SPACE_SEPARATOR_PATTERN = Pattern.compile(" +");

  /**
   * The test class on which the test method was found. Note that this is not
   * necessarily the declaring class.
   */
  protected ITestClass m_testClass;

  protected final transient Class<?> m_methodClass;
  protected final transient ConstructorOrMethod m_method;
  private transient String m_signature;
  protected String m_id = "";
  protected long m_date = -1;
  protected final transient IAnnotationFinder m_annotationFinder;
  protected String[] m_groups = {};
  protected String[] m_groupsDependedUpon = {};
  protected String[] m_methodsDependedUpon = {};
  protected String[] m_beforeGroups = {};
  protected String[] m_afterGroups = {};
  private boolean m_isAlwaysRun;
  private boolean m_enabled;

  private final String m_methodName;
  // If a depended group is not found
  private String m_missingGroup;
  private String m_description = null;
  protected AtomicInteger m_currentInvocationCount = new AtomicInteger(0);
  private int m_parameterInvocationCount = 1;
  private IRetryAnalyzer m_retryAnalyzer = null;
  private boolean m_skipFailedInvocations = true;
  private long m_invocationTimeOut = 0L;

  private List<Integer> m_invocationNumbers = Lists.newArrayList();
  private final List<Integer> m_failedInvocationNumbers = Collections.synchronizedList(Lists.<Integer>newArrayList());
  private long m_timeOut = 0;

  private boolean m_ignoreMissingDependencies;
  private int m_priority;

  private XmlTest m_xmlTest;
  private Object m_instance;

  /**
   * Constructs a <code>BaseTestMethod</code> TODO cquezel JavaDoc.
   *
   * @param method
   * @param annotationFinder
   * @param instance 
   */
  public BaseTestMethod(String methodName, Method method, IAnnotationFinder annotationFinder, Object instance) {
    this(methodName, new ConstructorOrMethod(method), annotationFinder, instance);
  }

  public BaseTestMethod(String methodName, ConstructorOrMethod com, IAnnotationFinder annotationFinder,
      Object instance) {
    m_methodClass = com.getDeclaringClass();
    m_method = com;
    m_methodName = methodName;
    m_annotationFinder = annotationFinder;
    m_instance = instance;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAlwaysRun() {
    return m_isAlwaysRun;
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @param alwaysRun
   */
  protected void setAlwaysRun(boolean alwaysRun) {
    m_isAlwaysRun = alwaysRun;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<?> getRealClass() {
    return m_methodClass;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ITestClass getTestClass() {
    return m_testClass;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setTestClass(ITestClass tc) {
    assert null != tc;
    if (! tc.getRealClass().equals(m_method.getDeclaringClass())) {
      assert m_method.getDeclaringClass().isAssignableFrom(tc.getRealClass()) :
        "\nMISMATCH : " + tc.getRealClass() + " " + m_method.getDeclaringClass();
    }
    m_testClass = tc;
  }

  @Override
  public int compareTo(Object o) {
    int result = -2;
    Class<?> thisClass = getRealClass();
    Class<?> otherClass = ((ITestNGMethod) o).getRealClass();
    if (this == o) {
      result = 0;
    } else if (thisClass.isAssignableFrom(otherClass)) {
      result = -1;
    } else if (otherClass.isAssignableFrom(thisClass)) {
      result = 1;
    } else if (equals(o)) {
      result = 0;
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Method getMethod() {
    return m_method.getMethod();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMethodName() {
    return m_methodName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object[] getInstances() {
    return new Object[] { getInstance() };
  }

  @Override
  public Object getInstance() {
    return m_instance;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long[] getInstanceHashCodes() {
    return m_testClass.getInstanceHashCodes();
  }

  /**
   * {@inheritDoc}
   * @return the addition of groups defined on the class and on this method.
   */
  @Override
  public String[] getGroups() {
    return m_groups;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getGroupsDependedUpon() {
    return m_groupsDependedUpon;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getMethodsDependedUpon() {
    return m_methodsDependedUpon;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTest() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBeforeSuiteConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAfterSuiteConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBeforeTestConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAfterTestConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBeforeGroupsConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAfterGroupsConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBeforeClassConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAfterClassConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBeforeMethodConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAfterMethodConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTimeOut() {
    long result = m_timeOut != 0 ? m_timeOut : (m_xmlTest != null ? m_xmlTest.getTimeOut(0) : 0);
    return result;
  }

  @Override
  public void setTimeOut(long timeOut) {
    m_timeOut = timeOut;
  }

  /**
   * {@inheritDoc}
   * @return the number of times this method needs to be invoked.
   */
  @Override
  public int getInvocationCount() {
    return 1;
  }

  /**
   * No-op.
   */
  @Override
  public void setInvocationCount(int counter) {
  }

  /**
   * {@inheritDoc}
   * @return the number of times this method or one of its clones must be invoked.
   */
  @Override
  public int getTotalInvocationCount() {
    return 1;
  }

  /**
   * {@inheritDoc} Default value for successPercentage.
   */
  @Override
  public int getSuccessPercentage() {
    return 100;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId() {
    return m_id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setId(String id) {
    m_id = id;
  }


  /**
   * {@inheritDoc}
   * @return Returns the date.
   */
  @Override
  public long getDate() {
    return m_date;
  }

  /**
   * {@inheritDoc}
   * @param date The date to set.
   */
  @Override
  public void setDate(long date) {
    m_date = date;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canRunFromClass(IClass testClass) {
    return m_methodClass.isAssignableFrom(testClass.getRealClass());
  }

  /**
   * {@inheritDoc} Compares two BaseTestMethod using the test class then the associated
   * Java Method.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    BaseTestMethod other = (BaseTestMethod) obj;

    boolean isEqual = m_testClass == null ? other.m_testClass == null
        : other.m_testClass != null &&
          m_testClass.getRealClass().equals(other.m_testClass.getRealClass())
          && m_instance == other.getInstance();

    return isEqual && getConstructorOrMethod().equals(other.getConstructorOrMethod());
  }

  /**
   * {@inheritDoc} This implementation returns the associated Java Method's hash code.
   * @return the associated Java Method's hash code.
   */
  @Override
  public int hashCode() {
    return m_method.hashCode();
  }

  protected void initGroups(Class<? extends ITestOrConfiguration> annotationClass) {
    //
    // Init groups
    //
    {
      ITestOrConfiguration annotation = getAnnotationFinder().findAnnotation(getMethod(), annotationClass);
      ITestOrConfiguration classAnnotation = getAnnotationFinder().findAnnotation(getMethod().getDeclaringClass(), annotationClass);

      setGroups(getStringArray(null != annotation ? annotation.getGroups() : null,
          null != classAnnotation ? classAnnotation.getGroups() : null));
    }

    //
    // Init groups depended upon
    //
    {
      ITestOrConfiguration annotation = getAnnotationFinder().findAnnotation(getMethod(), annotationClass);
      ITestOrConfiguration classAnnotation = getAnnotationFinder().findAnnotation(getMethod().getDeclaringClass(), annotationClass);

      Map<String, Set<String>> xgd = calculateXmlGroupDependencies(m_xmlTest);
      List<String> xmlGroupDependencies = Lists.newArrayList();
      for (String g : getGroups()) {
        Set<String> gdu = xgd.get(g);
        if (gdu != null) {
          xmlGroupDependencies.addAll(gdu);
        }
      }
      setGroupsDependedUpon(
          getStringArray(null != annotation ? annotation.getDependsOnGroups() : null,
          null != classAnnotation ? classAnnotation.getDependsOnGroups() : null),
          xmlGroupDependencies);

      String[] methodsDependedUpon =
        getStringArray(null != annotation ? annotation.getDependsOnMethods() : null,
        null != classAnnotation ? classAnnotation.getDependsOnMethods() : null);
      // Qualify these methods if they don't have a package
      for (int i = 0; i < methodsDependedUpon.length; i++) {
        String m = methodsDependedUpon[i];
        if (!m.contains(".")) {
          m = MethodHelper.calculateMethodCanonicalName(m_methodClass, methodsDependedUpon[i]);
          methodsDependedUpon[i] = m != null ? m : methodsDependedUpon[i];
        }
      }
      setMethodsDependedUpon(methodsDependedUpon);
    }
  }


  private static Map<String, Set<String>> calculateXmlGroupDependencies(XmlTest xmlTest) {
    Map<String, Set<String>> result = Maps.newHashMap();
    if (xmlTest == null) {
      return result;
    }

    for (Map.Entry<String, String> e : xmlTest.getXmlDependencyGroups().entrySet()) {
      String name = e.getKey();
      String dependsOn = e.getValue();
      Set<String> set = result.get(name);
      if (set == null) {
        set = Sets.newHashSet();
        result.put(name, set);
      }
      set.addAll(Arrays.asList(SPACE_SEPARATOR_PATTERN.split(dependsOn)));
    }

    return result;
  }

  protected IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  protected IClass getIClass() {
    return m_testClass;
  }

  private String computeSignature() {
    String classLong = m_method.getDeclaringClass().getName();
    String cls = classLong.substring(classLong.lastIndexOf(".") + 1);
    StringBuilder result = new StringBuilder(cls).append(".").append(m_method.getName()).append("(");
    int i = 0;
    for (Class<?> p : m_method.getParameterTypes()) {
      if (i++ > 0) {
        result.append(", ");
      }
      result.append(p.getName());
    }
    result.append(")");
    result.append("[pri:").append(getPriority()).append(", instance:").append(m_instance).append("]");

    return result.toString();
  }

  protected String getSignature() {
    if (m_signature == null) {
      m_signature = computeSignature();
    }
    return m_signature;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getSignature();
  }

  protected String[] getStringArray(String[] methodArray, String[] classArray) {
    final Set<String> vResult = Sets.newHashSet();
    if (null != methodArray) {
      Collections.addAll(vResult, methodArray);
    }
    if (null != classArray) {
      Collections.addAll(vResult, classArray);
    }
    return vResult.toArray(new String[vResult.size()]);
  }

  protected void setGroups(String[] groups) {
    m_groups = groups;
  }

  protected void setGroupsDependedUpon(String[] groups, Collection<String> xmlGroupDependencies) {
    List<String> l = Lists.newArrayList();
    l.addAll(Arrays.asList(groups));
    l.addAll(xmlGroupDependencies);
    m_groupsDependedUpon = l.toArray(new String[l.size()]);
  }

  protected void setMethodsDependedUpon(String[] methods) {
    m_methodsDependedUpon = methods;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addMethodDependedUpon(String method) {
    String[] newMethods = new String[m_methodsDependedUpon.length + 1];
    newMethods[0] = method;
    System.arraycopy(m_methodsDependedUpon, 0, newMethods, 1, m_methodsDependedUpon.length);
    m_methodsDependedUpon = newMethods;
  }

  private static void ppp(String s) {
    System.out.println("[BaseTestMethod] " + s);
  }

  /** Compares two ITestNGMethod by date. */
  public static final Comparator<?> DATE_COMPARATOR = new Comparator<Object>() {
    @Override
    public int compare(Object o1, Object o2) {
      try {
        ITestNGMethod m1 = (ITestNGMethod) o1;
        ITestNGMethod m2 = (ITestNGMethod) o2;
        return (int) (m1.getDate() - m2.getDate());
      }
      catch(Exception ex) {
        return 0; // TODO CQ document this logic
      }
    }
  };

  /**
   * {@inheritDoc}
   */
  @Override
  public String getMissingGroup() {
    return m_missingGroup;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setMissingGroup(String group) {
    m_missingGroup = group;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getThreadPoolSize() {
    return 0;
  }

  /**
   * No-op.
   */
  @Override
  public void setThreadPoolSize(int threadPoolSize) {
  }

  @Override
  public void setDescription(String description) {
    m_description = description;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription() {
    return m_description;
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public boolean getEnabled() {
    return m_enabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getBeforeGroups() {
    return m_beforeGroups;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getAfterGroups() {
    return m_afterGroups;
  }

  @Override
  public void incrementCurrentInvocationCount() {
    m_currentInvocationCount.incrementAndGet();
  }

  @Override
  public int getCurrentInvocationCount() {
    return m_currentInvocationCount.get();
  }

  @Override
  public void setParameterInvocationCount(int n) {
    m_parameterInvocationCount = n;
  }

  @Override
  public int getParameterInvocationCount() {
    return m_parameterInvocationCount;
  }

  @Override
  public abstract ITestNGMethod clone();

  @Override
  public IRetryAnalyzer getRetryAnalyzer() {
    return m_retryAnalyzer;
  }

  @Override
  public void setRetryAnalyzer(IRetryAnalyzer retryAnalyzer) {
    m_retryAnalyzer = retryAnalyzer;
  }

  @Override
  public boolean skipFailedInvocations() {
    return m_skipFailedInvocations;
  }

  @Override
  public void setSkipFailedInvocations(boolean s) {
    m_skipFailedInvocations = s;
  }

  public void setInvocationTimeOut(long timeOut) {
    m_invocationTimeOut = timeOut;
  }

  @Override
  public long getInvocationTimeOut() {
    return m_invocationTimeOut;
  }

  @Override
  public boolean ignoreMissingDependencies() {
    return m_ignoreMissingDependencies;
  }

  @Override
  public void setIgnoreMissingDependencies(boolean i) {
    m_ignoreMissingDependencies = i;
  }

  @Override
  public List<Integer> getInvocationNumbers() {
    return m_invocationNumbers;
  }

  @Override
  public void setInvocationNumbers(List<Integer> numbers) {
    m_invocationNumbers = numbers;
  }

  @Override
  public List<Integer> getFailedInvocationNumbers() {
    return m_failedInvocationNumbers;
  }

  @Override
  public void addFailedInvocationNumber(int number) {
    m_failedInvocationNumbers.add(number);
  }

  @Override
  public int getPriority() {
    return m_priority;
  }

  @Override
  public void setPriority(int priority) {
    m_priority = priority;
  }

  @Override
  public XmlTest getXmlTest() {
    return m_xmlTest;
  }

  public void setXmlTest(XmlTest xmlTest) {
    m_xmlTest = xmlTest;
  }

  @Override
  public ConstructorOrMethod getConstructorOrMethod() {
    return m_method;
  }

  @Override
  public Map<String, String> findMethodParameters(XmlTest test) {
    // Get the test+suite parameters
    Map<String, String> result = test.getAllParameters();
    for (XmlClass xmlClass: test.getXmlClasses()) {
      if (xmlClass.getName().equals(getTestClass().getName())) {
        result.putAll(xmlClass.getLocalParameters());
        for (XmlInclude include : xmlClass.getIncludedMethods()) {
          if (include.getName().equals(getMethodName())) {
            result.putAll(include.getLocalParameters());
            break;
          }
        }
      }
    }

    return result;
  }
}
