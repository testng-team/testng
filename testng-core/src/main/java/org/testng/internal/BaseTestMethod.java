package org.testng.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.testng.IClass;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.annotations.DisabledRetryAnalyzer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.IInvocationStatus;
import org.testng.internal.objects.Dispenser;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.xml.XmlTest;

/** Superclass to represent both &#64;Test and &#64;Configuration methods. */
public abstract class BaseTestMethod implements ITestNGMethod, IInvocationStatus {

  private static final Pattern SPACE_SEPARATOR_PATTERN = Pattern.compile(" +");

  /**
   * The test class on which the test method was found. Note that this is not necessarily the
   * declaring class.
   */
  protected ITestClass m_testClass;

  protected final Class<?> m_methodClass;
  protected final ConstructorOrMethod m_method;
  private String m_signature;
  protected String m_id = "";
  protected long m_date = -1;
  protected final IAnnotationFinder m_annotationFinder;
  protected String[] m_groups = {};
  protected String[] m_groupsDependedUpon = {};
  protected String[] m_methodsDependedUpon = {};
  protected String[] m_beforeGroups = {};
  protected String[] m_afterGroups = {};
  private boolean m_isAlwaysRun;
  private boolean m_enabled;

  private final String m_methodName;
  // If a depends on group is not found
  private String m_missingGroup;
  private String m_description = null;
  protected AtomicInteger m_currentInvocationCount = new AtomicInteger(0);
  private int m_parameterInvocationCount = 1;
  private Callable<Boolean> m_moreInvocationChecker;
  private IRetryAnalyzer m_retryAnalyzer = null;
  private Class<? extends IRetryAnalyzer> m_retryAnalyzerClass = null;
  private boolean m_skipFailedInvocations = true;
  private long m_invocationTimeOut = 0L;

  private List<Integer> m_invocationNumbers = Lists.newArrayList();
  private final Set<ITestNGMethod> downstreamDependencies = Sets.newHashSet();
  private final Set<ITestNGMethod> upstreamDependencies = Sets.newHashSet();
  private final Collection<Integer> m_failedInvocationNumbers = new ConcurrentLinkedQueue<>();
  private long m_timeOut = 0;

  private boolean m_ignoreMissingDependencies;
  private int m_priority;
  private int m_interceptedPriority;

  private XmlTest m_xmlTest;
  private final Object m_instance;

  private final Map<String, IRetryAnalyzer> m_testMethodToRetryAnalyzer = Maps.newConcurrentMap();
  protected final ITestObjectFactory m_objectFactory;

  public BaseTestMethod(
      ITestObjectFactory objectFactory,
      String methodName,
      ConstructorOrMethod com,
      IAnnotationFinder annotationFinder,
      Object instance) {
    m_objectFactory = objectFactory;
    m_methodClass = com.getDeclaringClass();
    m_method = com;
    m_methodName = methodName;
    m_annotationFinder = annotationFinder;
    m_instance = instance;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAlwaysRun() {
    return m_isAlwaysRun;
  }

  protected void setAlwaysRun(boolean alwaysRun) {
    m_isAlwaysRun = alwaysRun;
  }

  /** {@inheritDoc} */
  @Override
  public Class<?> getRealClass() {
    return m_methodClass;
  }

  /** {@inheritDoc} */
  @Override
  public ITestClass getTestClass() {
    return m_testClass;
  }

  /** {@inheritDoc} */
  @Override
  public void setTestClass(ITestClass tc) {
    if (tc == null) {
      throw new IllegalArgumentException("test class cannot be null");
    }
    boolean assignable = m_method.getDeclaringClass().isAssignableFrom(tc.getRealClass());
    if (!assignable) {
      throw new IllegalArgumentException(
          "mismatch in classes between "
              + tc.getName()
              + " and "
              + m_method.getDeclaringClass().getName());
    }

    m_testClass = tc;
  }

  /** {@inheritDoc} */
  @Override
  public String getMethodName() {
    return m_methodName;
  }

  @Override
  public Object getInstance() {
    return IParameterInfo.embeddedInstance(m_instance);
  }

  /** {@inheritDoc} */
  @Override
  public long[] getInstanceHashCodes() {
    return m_testClass.getInstanceHashCodes();
  }

  /**
   * {@inheritDoc}
   *
   * @return the addition of groups defined on the class and on this method.
   */
  @Override
  public String[] getGroups() {
    return m_groups;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getGroupsDependedUpon() {
    return m_groupsDependedUpon;
  }

  /** {@inheritDoc} */
  @Override
  public String[] getMethodsDependedUpon() {
    return m_methodsDependedUpon;
  }

  @Override
  public Set<ITestNGMethod> downstreamDependencies() {
    return Collections.unmodifiableSet(downstreamDependencies);
  }

  @Override
  public Set<ITestNGMethod> upstreamDependencies() {
    return Collections.unmodifiableSet(upstreamDependencies);
  }

  public void setDownstreamDependencies(Set<ITestNGMethod> methods) {
    if (!downstreamDependencies.isEmpty()) {
      downstreamDependencies.clear();
    }
    Set<ITestNGMethod> toAdd = methods;
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      toAdd = methods.stream().map(LiteWeightTestNGMethod::new).collect(Collectors.toSet());
    }
    downstreamDependencies.addAll(toAdd);
  }

  public void setUpstreamDependencies(Set<ITestNGMethod> methods) {
    if (!upstreamDependencies.isEmpty()) {
      upstreamDependencies.clear();
    }
    Set<ITestNGMethod> toAdd = methods;
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      toAdd = methods.stream().map(LiteWeightTestNGMethod::new).collect(Collectors.toSet());
    }
    upstreamDependencies.addAll(toAdd);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isTest() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBeforeSuiteConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAfterSuiteConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBeforeTestConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAfterTestConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBeforeGroupsConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAfterGroupsConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBeforeClassConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAfterClassConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBeforeMethodConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isAfterMethodConfiguration() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public long getTimeOut() {
    return m_timeOut != 0 ? m_timeOut : (m_xmlTest != null ? m_xmlTest.getTimeOut(0) : 0);
  }

  @Override
  public void setTimeOut(long timeOut) {
    m_timeOut = timeOut;
  }

  /**
   * {@inheritDoc}
   *
   * @return the number of times this method needs to be invoked.
   */
  @Override
  public int getInvocationCount() {
    return 1;
  }

  /** No-op. */
  @Override
  public void setInvocationCount(int counter) {}

  /** {@inheritDoc} Default value for successPercentage. */
  @Override
  public int getSuccessPercentage() {
    return 100;
  }

  /** {@inheritDoc} */
  @Override
  public String getId() {
    return m_id;
  }

  /** {@inheritDoc} */
  @Override
  public void setId(String id) {
    m_id = id;
  }

  /**
   * {@inheritDoc}
   *
   * @return Returns the date.
   */
  @Override
  public long getDate() {
    return m_date;
  }

  /**
   * {@inheritDoc}
   *
   * @param date The date to set.
   */
  @Override
  public void setDate(long date) {
    m_date = date;
  }

  /** {@inheritDoc} */
  @Override
  public boolean canRunFromClass(IClass testClass) {
    return m_methodClass.isAssignableFrom(testClass.getRealClass());
  }

  /**
   * {@inheritDoc} Compares two BaseTestMethod using the test class then the associated Java Method.
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

    boolean isEqual =
        m_testClass == null
            ? other.m_testClass == null
            : other.m_testClass != null
                && m_testClass.getRealClass().equals(other.m_testClass.getRealClass())
                && getInstance() == other.getInstance();

    return isEqual && getConstructorOrMethod().equals(other.getConstructorOrMethod());
  }

  /**
   * {@inheritDoc} This implementation returns the associated Java Method's hash code.
   *
   * @return the associated Java Method's hash code.
   */
  @Override
  public int hashCode() {
    int hash = m_method.hashCode();
    if (m_instance != null) {
      hash = hash * 31 + System.identityHashCode(m_instance);
    }
    return hash;
  }

  protected void initGroups(Class<? extends ITestOrConfiguration> annotationClass) {
    ITestOrConfiguration annotation =
        getAnnotationFinder().findAnnotation(getConstructorOrMethod(), annotationClass);
    Object object = getInstance();
    Class<?> clazz = getConstructorOrMethod().getDeclaringClass();
    if (object != null) {
      clazz = object.getClass();
    }
    ITestOrConfiguration classAnnotation =
        getAnnotationFinder().findAnnotation(clazz, annotationClass);

    setGroups(
        getStringArray(
            null != annotation ? annotation.getGroups() : null,
            null != classAnnotation ? classAnnotation.getGroups() : null));

    initRestOfGroupDependencies(annotationClass);
  }

  protected void initBeforeAfterGroups(
      Class<? extends ITestOrConfiguration> annotationClass, String[] groups) {
    String[] groupsAtMethodLevel =
        calculateGroupsToUseConsideringValuesAndGroupValues(annotationClass, groups);
    // @BeforeGroups and @AfterGroups annotation cannot be used at Class level. So its always null
    setGroups(getStringArray(groupsAtMethodLevel, null));
    initRestOfGroupDependencies(annotationClass);
  }

  private String[] calculateGroupsToUseConsideringValuesAndGroupValues(
      Class<? extends ITestOrConfiguration> annotationClass, String[] groups) {
    if (groups == null || groups.length == 0) {
      ITestOrConfiguration annotation =
          getAnnotationFinder().findAnnotation(getConstructorOrMethod(), annotationClass);
      groups = null != annotation ? annotation.getGroups() : null;
    }
    return groups;
  }

  private void initRestOfGroupDependencies(Class<? extends ITestOrConfiguration> annotationClass) {
    //
    // Init groups depended upon
    //
    ITestOrConfiguration annotation =
        getAnnotationFinder().findAnnotation(getConstructorOrMethod(), annotationClass);
    ITestOrConfiguration classAnnotation =
        getAnnotationFinder()
            .findAnnotation(getConstructorOrMethod().getDeclaringClass(), annotationClass);

    Map<String, Set<String>> xgd = calculateXmlGroupDependencies(m_xmlTest);
    List<String> xmlGroupDependencies = Lists.newArrayList();
    for (String g : getGroups()) {
      Set<String> gdu = xgd.get(g);
      if (gdu != null) {
        xmlGroupDependencies.addAll(gdu);
      }
    }
    setGroupsDependedUpon(
        getStringArray(
            null != annotation ? annotation.getDependsOnGroups() : null,
            null != classAnnotation ? classAnnotation.getDependsOnGroups() : null),
        xmlGroupDependencies);

    String[] methodsDependedUpon =
        getStringArray(
            null != annotation ? annotation.getDependsOnMethods() : null,
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

  private static Map<String, Set<String>> calculateXmlGroupDependencies(XmlTest xmlTest) {
    Map<String, Set<String>> result = Maps.newHashMap();
    if (xmlTest == null) {
      return result;
    }

    for (Map.Entry<String, String> e : xmlTest.getXmlDependencyGroups().entrySet()) {
      String name = e.getKey();
      String dependsOn = e.getValue();
      Set<String> set = result.computeIfAbsent(name, s -> Sets.newHashSet());
      set.addAll(Arrays.asList(SPACE_SEPARATOR_PATTERN.split(dependsOn)));
    }

    return result;
  }

  protected IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  static StringBuilder stringify(String cls, ConstructorOrMethod method) {
    StringBuilder result = new StringBuilder(cls).append(".").append(method.getName()).append("(");
    return result.append(method.stringifyParameterTypes()).append(")");
  }

  private String computeSignature() {
    String classLong = m_method.getDeclaringClass().getName();
    String cls = classLong.substring(classLong.lastIndexOf(".") + 1);
    StringBuilder result = stringify(cls, m_method);
    result
        .append("[pri:")
        .append(getPriority())
        .append(", instance:")
        .append(getInstance())
        .append(instanceParameters())
        .append(customAttributes())
        .append("]");

    return result.toString();
  }

  private String customAttributes() {
    CustomAttribute[] attributes = getAttributes();
    if (attributes == null || attributes.length == 0) {
      return "";
    }
    return ", attributes: "
        + Arrays.stream(this.getAttributes())
            .map(
                attribute ->
                    "<name: "
                        + attribute.name()
                        + ", value:"
                        + Arrays.toString(attribute.values())
                        + ">")
            .collect(Collectors.joining(", "));
  }

  public String getSimpleName() {
    return m_method.getDeclaringClass().getSimpleName() + "." + m_method.getName();
  }

  private String instanceParameters() {
    IParameterInfo instance = getFactoryMethodParamsInfo();
    if (instance != null) {
      return ", instance params:" + Arrays.toString(instance.getParameters());
    }
    return "";
  }

  protected String getSignature() {
    if (m_signature == null) {
      m_signature = computeSignature();
    }
    return m_signature;
  }

  /** {@inheritDoc} */
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
    return vResult.toArray(new String[0]);
  }

  protected void setGroups(String[] groups) {
    m_groups = groups;
  }

  protected void setGroupsDependedUpon(String[] groups, Collection<String> xmlGroupDependencies) {
    List<String> l = Lists.newArrayList();
    l.addAll(Arrays.asList(groups));
    l.addAll(xmlGroupDependencies);
    m_groupsDependedUpon = l.toArray(new String[0]);
  }

  protected void setMethodsDependedUpon(String[] methods) {
    m_methodsDependedUpon = methods;
  }

  /** {@inheritDoc} */
  @Override
  public void addMethodDependedUpon(String method) {
    String[] newMethods = new String[m_methodsDependedUpon.length + 1];
    newMethods[0] = method;
    System.arraycopy(m_methodsDependedUpon, 0, newMethods, 1, m_methodsDependedUpon.length);
    m_methodsDependedUpon = newMethods;
  }

  /** {@inheritDoc} */
  @Override
  public String getMissingGroup() {
    return m_missingGroup;
  }

  /** {@inheritDoc} */
  @Override
  public void setMissingGroup(String group) {
    m_missingGroup = group;
  }

  /** {@inheritDoc} */
  @Override
  public int getThreadPoolSize() {
    return 0;
  }

  /** No-op. */
  @Override
  public void setThreadPoolSize(int threadPoolSize) {}

  @Override
  public void setDescription(String description) {
    m_description = description;
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public String[] getBeforeGroups() {
    return m_beforeGroups;
  }

  /** {@inheritDoc} */
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
  public void setMoreInvocationChecker(Callable<Boolean> moreInvocationChecker) {
    m_moreInvocationChecker = moreInvocationChecker;
  }

  @Override
  public boolean hasMoreInvocation() {
    if (m_moreInvocationChecker != null) {
      try {
        return m_moreInvocationChecker.call();
      } catch (Exception e) {
        // Should never append
        throw new RuntimeException(e);
      }
    }
    return getCurrentInvocationCount() < getInvocationCount() * getParameterInvocationCount();
  }

  @Override
  public abstract ITestNGMethod clone();

  @Override
  public IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
    return getRetryAnalyzerConsideringMethodParameters(result);
  }

  @Override
  public void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz) {
    m_retryAnalyzerClass = clazz == null ? DisabledRetryAnalyzer.class : clazz;
  }

  @Override
  public Class<? extends IRetryAnalyzer> getRetryAnalyzerClass() {
    return m_retryAnalyzerClass;
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
    return new ArrayList<>(m_failedInvocationNumbers);
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
  public int getInterceptedPriority() {
    return m_interceptedPriority;
  }

  @Override
  public void setInterceptedPriority(int priority) {
    m_interceptedPriority = priority;
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
  public Class<?>[] getParameterTypes() {
    return m_method.getParameterTypes();
  }

  @Override
  public Map<String, String> findMethodParameters(XmlTest test) {
    return XmlTestUtils.findMethodParameters(test, getTestClass().getName(), getMethodName());
  }

  @Override
  public String getQualifiedName() {
    return getRealClass().getName() + "." + getMethodName();
  }

  @Override
  public IParameterInfo getFactoryMethodParamsInfo() {
    if (m_instance instanceof IParameterInfo) {
      return (IParameterInfo) m_instance;
    }
    return null;
  }

  private long invocationTime;

  @Override
  public void setInvokedAt(long date) {
    this.invocationTime = date;
  }

  @Override
  public long getInvocationTime() {
    return invocationTime;
  }

  private IRetryAnalyzer getRetryAnalyzerConsideringMethodParameters(ITestResult tr) {
    if (this.m_retryAnalyzerClass.equals(DisabledRetryAnalyzer.class)) {
      return null;
    }
    if (isNotParameterisedTest(tr)) {
      this.m_retryAnalyzer = computeRetryAnalyzerInstanceToUse(tr);
      return this.m_retryAnalyzer;
    }
    final String keyAsString = getSimpleName() + "#" + getParameterInvocationCount();
    return m_testMethodToRetryAnalyzer.computeIfAbsent(
        keyAsString,
        key -> {
          BasicAttributes ba = new BasicAttributes(null, this.m_retryAnalyzerClass);
          CreationAttributes attributes = new CreationAttributes(tr.getTestContext(), ba, null);
          return (IRetryAnalyzer) Dispenser.newInstance(m_objectFactory).dispense(attributes);
        });
  }

  private static boolean isNotParameterisedTest(ITestResult tr) {
    return Optional.ofNullable(tr.getParameters()).orElse(new Object[0]).length == 0;
  }

  private IRetryAnalyzer computeRetryAnalyzerInstanceToUse(ITestResult tr) {
    if (m_retryAnalyzer != null) {
      return m_retryAnalyzer;
    }
    if (m_retryAnalyzerClass.equals(DisabledRetryAnalyzer.class)) {
      return null;
    }
    BasicAttributes ba = new BasicAttributes(null, this.m_retryAnalyzerClass);
    CreationAttributes attributes = new CreationAttributes(tr.getTestContext(), ba, null);
    return (IRetryAnalyzer) Dispenser.newInstance(m_objectFactory).dispense(attributes);
  }
}
