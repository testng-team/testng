package org.testng.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.IFactoryInstance;
import org.testng.IRetryAnalyzer;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.ITestResult;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.internal.annotations.DisabledRetryAnalyzer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.IInvocationStatus;
import org.testng.internal.objects.Dispenser;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

/** Superclass to represent both &#64;Test and &#64;Configuration methods. */
public abstract class BaseTestMethod
    implements ITestNGMethod, IInvocationStatus, IInstanceIdentity {

  private static final Pattern SPACE_SEPARATOR_PATTERN = Pattern.compile(" +");

  /**
   * Shared stand-in for the group arrays below when they are empty, which is the common case. A
   * {@code String[] x = {}} field initializer allocates a fresh array per instance; a big
   * &#64;Factory suite builds one method object per instance, so those add up to five throwaway
   * arrays per method. A zero-length array cannot be mutated, so handing the same one to every
   * method is safe even though the getters return it directly.
   */
  static final String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * The test class on which the test method was found. Note that this is not necessarily the
   * declaring class.
   */
  protected @Nullable ITestClass m_testClass;

  protected final Class<?> m_methodClass;
  protected final ConstructorOrMethod m_method;
  private @Nullable String m_signature;
  protected String m_id = "";
  protected long m_date = -1;
  protected final IAnnotationFinder m_annotationFinder;
  protected String[] m_groups = EMPTY_STRING_ARRAY;
  protected String[] m_groupsDependedUpon = EMPTY_STRING_ARRAY;
  protected String[] m_methodsDependedUpon = EMPTY_STRING_ARRAY;
  protected String[] m_beforeGroups = EMPTY_STRING_ARRAY;
  protected String[] m_afterGroups = EMPTY_STRING_ARRAY;
  private boolean m_isAlwaysRun;
  private boolean m_enabled;

  private final String m_methodName;
  // If a depends on group is not found
  private @Nullable String m_missingGroup;
  private @Nullable String m_description = null;
  protected AtomicInteger m_currentInvocationCount = new AtomicInteger(0);
  private int m_parameterInvocationCount = 1;
  // Set on the per-invocation clones created for a parallel (threadPoolSize > 1)
  // invocationCount. For those clones the firstTimeOnly @BeforeMethod and the
  // lastTimeOnly @AfterMethod are run once - as a barrier - around the whole pool
  // instead of inside each parallel invocation, so the clones must not run them.
  private boolean m_skipFirstAndLastTimeOnlyConfigs;
  private @Nullable Callable<Boolean> m_moreInvocationChecker;
  private @Nullable IRetryAnalyzer m_retryAnalyzer = null;
  private Class<? extends IRetryAnalyzer> m_retryAnalyzerClass = DisabledRetryAnalyzer.class;
  private boolean m_skipFailedInvocations = true;
  private long m_invocationTimeOut = 0L;

  // Non-empty only for the <include invocation-numbers="..."> case. Starts out as the shared empty
  // list rather than a fresh one per method; setInvocationNumbers replaces it wholesale, and
  // nothing writes through the reference, so sharing an immutable list is safe.
  private List<Integer> m_invocationNumbers = Collections.emptyList();
  // Left null until the method actually has dependencies, which most methods never do. An empty
  // HashSet costs the set plus its backing HashMap, and a big @Factory suite holds two of them per
  // method per instance. Published to the worker threads by the volatile write in the setters.
  private volatile @Nullable Set<ITestNGMethod> downstreamDependencies;
  private volatile @Nullable Set<ITestNGMethod> upstreamDependencies;
  // Written only when an invocation fails. An empty ConcurrentLinkedQueue still costs the queue
  // plus the dummy node it starts with, so hold off until there is a failure to record.
  @SuppressWarnings("rawtypes")
  private volatile @Nullable Collection m_failedInvocationNumbers;

  // The @Nullable on the value type is what lets the compareAndSet below name null as the value it
  // expects to replace. Without it the package being @NullMarked makes the updater's value type
  // non-null, and NullAway rejects the call.
  @SuppressWarnings("rawtypes")
  private static final AtomicReferenceFieldUpdater<BaseTestMethod, @Nullable Collection>
      FAILED_INVOCATIONS =
          AtomicReferenceFieldUpdater.newUpdater(
              BaseTestMethod.class, Collection.class, "m_failedInvocationNumbers");

  private long m_timeOut = 0;

  private boolean m_ignoreMissingDependencies;
  private int m_priority;
  private int m_interceptedPriority;

  private @Nullable XmlTest m_xmlTest;
  // The <class> and <include> tags this method was scheduled for. Both tags may be repeated, and
  // each occurrence carries its own parameters, so the tag -- not its name -- is what answers
  // findMethodParameters. Null when no tag named the method: a @Factory produced class, a method
  // pulled in by the group transitive closure, a suite built without <methods>.
  private @Nullable XmlClass m_xmlClass;
  private @Nullable XmlInclude m_xmlInclude;
  // Which of those occurrences this is, counted within this method and instance. Folded into
  // equals/hashCode so that two repeats of the same tag stay distinct nodes of the method graph;
  // XmlClass#getIndex and XmlInclude#getIndex cannot serve, being left at zero by every suite the
  // XML content handler did not parse.
  private int m_xmlOccurrenceIndex;
  private final IObject.@Nullable IdentifiableObject m_instance;

  // Only ever populated for a parameterised test that has a retry analyzer, so it stays null for
  // almost every method. Installed with a CAS through the updater below rather than under a lock:
  // a per-instance lock object would give back a quarter of what leaving the map out saves.
  @SuppressWarnings("rawtypes")
  private volatile @Nullable ConcurrentHashMap m_testMethodToRetryAnalyzer;

  // @Nullable value type for the same reason as FAILED_INVOCATIONS above.
  @SuppressWarnings("rawtypes")
  private static final AtomicReferenceFieldUpdater<BaseTestMethod, @Nullable ConcurrentHashMap>
      RETRY_ANALYZERS =
          AtomicReferenceFieldUpdater.newUpdater(
              BaseTestMethod.class, ConcurrentHashMap.class, "m_testMethodToRetryAnalyzer");

  protected final ITestObjectFactory m_objectFactory;

  public BaseTestMethod(
      ITestObjectFactory objectFactory,
      String methodName,
      ConstructorOrMethod com,
      IAnnotationFinder annotationFinder,
      IObject.@Nullable IdentifiableObject instance) {
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
  public @Nullable ITestClass getTestClass() {
    return m_testClass;
  }

  /** {@inheritDoc} */
  @Override
  public void setTestClass(@Nullable ITestClass tc) {
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
  public @Nullable Object getInstance() {
    // Hot path (called per invocation via TestNgMethodUtils.isSameInstance): a plain null-guarded
    // chain instead of Optional.ofNullable(...).map(...).map(...), which allocated three throwaway
    // Optionals on every call. embeddedInstance passes null straight through, so the result is
    // unchanged.
    if (m_instance == null) {
      return null;
    }
    return IParameterInfo.embeddedInstance(m_instance.getInstance());
  }

  /**
   * @return - {@code true} unless this method is bound to a lazy {@code @Factory} instance that has
   *     not been created yet. Callers use this to avoid instantiating a lazy instance (e.g. to
   *     build a diagnostic message) before its test is due to run. Reading this never triggers
   *     creation.
   */
  public boolean isInstanceInstantiated() {
    IParameterInfo info = getFactoryParameterInfo();
    return info == null || info.isInstanceInstantiated();
  }

  @Override
  public @Nullable UUID getInstanceId() {
    return m_instance == null ? null : m_instance.getInstanceId();
  }

  /** {@inheritDoc} */
  @Override
  public long[] getInstanceHashCodes() {
    return IObject.objectHashCodes(m_testClass);
  }

  /**
   * The instance wrapper a clone of this method should carry: the same identity, or {@code null}
   * when this method carries no instance at all.
   */
  protected IObject.@Nullable IdentifiableObject cloneInstance() {
    Object instance = getInstance();
    UUID instanceId = getInstanceId();
    if (instance == null || instanceId == null) {
      return null;
    }
    return new IObject.IdentifiableObject(instance, instanceId);
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
    return readOnlyView(downstreamDependencies);
  }

  @Override
  public Set<ITestNGMethod> upstreamDependencies() {
    return readOnlyView(upstreamDependencies);
  }

  public void setDownstreamDependencies(Set<ITestNGMethod> methods) {
    downstreamDependencies = setupDependencies(methods);
  }

  public void setUpstreamDependencies(Set<ITestNGMethod> methods) {
    upstreamDependencies = setupDependencies(methods);
  }

  private static Set<ITestNGMethod> readOnlyView(@Nullable Set<ITestNGMethod> dependencies) {
    return dependencies == null
        ? Collections.emptySet()
        : Collections.unmodifiableSet(dependencies);
  }

  /**
   * @return a set holding {@code methods}, or {@code null} when there are none. Returning null
   *     rather than an empty set is what keeps a dependency-free method from carrying one.
   */
  private static @Nullable Set<ITestNGMethod> setupDependencies(Set<ITestNGMethod> methods) {
    if (methods.isEmpty()) {
      return null;
    }
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      return methods.stream().map(LiteWeightTestNGMethod::new).collect(Collectors.toSet());
    }
    return new HashSet<>(methods);
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

  @Override
  public Optional<IFactoryInstance> getFactoryInstance() {
    IParameterInfo info = getFactoryParameterInfo();
    return info == null ? Optional.empty() : Optional.ofNullable(info.getFactoryInstance());
  }

  /**
   * Returns the internal factory metadata this method is bound to.
   *
   * @return - The metadata, or {@code null}. Unlike the deprecated {@link
   *     #getFactoryMethodParamsInfo()} this is not part of {@link ITestNGMethod}, so the
   *     lazy-instantiation details stay available to TestNG without being published.
   */
  public @Nullable IParameterInfo getFactoryParameterInfo() {
    Object instance = m_instance == null ? null : m_instance.getInstance();
    return instance instanceof IParameterInfo ? (IParameterInfo) instance : null;
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
  // getClass() on purpose: none of ConfigurationMethod, FactoryMethod and TestNGMethod overrides
  // equals, so this comparison is the only thing that tells them apart when they wrap the same
  // method, the same class and the same instance id -- and they are HashSet members and HashMap
  // keys in a dozen places. Nothing in the suite fails if it is changed to instanceof, which is
  // why this is a decision rather than a bug. The class cannot be sealed either: those three
  // extend it here and org.testng.internal is Export-Package'd.
  @SuppressWarnings("EqualsGetClass")
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
                // Compare by per-instance id rather than the instantiated instance, so equality
                // checks (heavily used while building the method graph) never force a lazy
                // @Factory instance to be created.
                && Objects.equals(getInstanceId(), other.getInstanceId());

    return isEqual
        && m_xmlOccurrenceIndex == other.m_xmlOccurrenceIndex
        && getConstructorOrMethod().equals(other.getConstructorOrMethod());
  }

  /**
   * {@inheritDoc} This implementation returns the associated Java Method's hash code.
   *
   * @return the associated Java Method's hash code.
   */
  @Override
  public int hashCode() {
    int hash = m_method.hashCode();
    // Fold in the per-instance id rather than the instantiated instance's identity hash. This keeps
    // hashCode consistent with equals (which compares instance ids) and, crucially, never forces a
    // lazy @Factory instance to be created while methods sit in hash-based collections.
    UUID instanceId = getInstanceId();
    if (instanceId != null) {
      hash = hash * 31 + instanceId.hashCode();
    }
    return hash * 31 + m_xmlOccurrenceIndex;
  }

  protected void initGroups(Class<? extends ITestOrConfiguration> annotationClass) {
    ITestOrConfiguration annotation =
        getAnnotationFinder().findAnnotation(getConstructorOrMethod(), annotationClass);
    Class<?> clazz = getConstructorOrMethod().getDeclaringClass();
    if (isInstanceInstantiated()) {
      Object object = getInstance();
      if (object != null) {
        clazz = object.getClass();
      }
    }
    // else: a lazy @Factory instance is not created yet; a constructor factory produces exactly its
    // declaring class, which is already the default above — so don't instantiate to read the class.
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
    String @Nullable [] groupsAtMethodLevel =
        calculateGroupsToUseConsideringValuesAndGroupValues(annotationClass, groups);
    // @BeforeGroups and @AfterGroups annotation cannot be used at Class level. So its always null
    setGroups(getStringArray(groupsAtMethodLevel, null));
    initRestOfGroupDependencies(annotationClass);
  }

  private String @Nullable [] calculateGroupsToUseConsideringValuesAndGroupValues(
      Class<? extends ITestOrConfiguration> annotationClass, String @Nullable [] groups) {
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
    List<String> xmlGroupDependencies = new ArrayList<>();
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

  private static Map<String, Set<String>> calculateXmlGroupDependencies(@Nullable XmlTest xmlTest) {
    Map<String, Set<String>> result = new HashMap<>();
    if (xmlTest == null) {
      return result;
    }

    for (Map.Entry<String, String> e : xmlTest.getXmlDependencyGroups().entrySet()) {
      String name = e.getKey();
      String dependsOn = e.getValue();
      Set<String> set = result.computeIfAbsent(name, s -> new HashSet<>());
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
        // Don't instantiate a lazy @Factory instance just to render a signature; the factory
        // parameters appended below already identify the instance.
        .append(isInstanceInstantiated() ? String.valueOf(getInstance()) : "<uninstantiated>")
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
    return getFactoryInstance()
        .map(it -> ", instance params:" + Arrays.toString(it.getParameters()))
        .orElse("");
  }

  protected String getSignature() {
    if (m_signature == null) {
      String signature = computeSignature();
      // Only memoize once the instance is stable; a signature computed while a lazy @Factory
      // instance is still uninstantiated would otherwise be cached and become stale after creation.
      if (isInstanceInstantiated()) {
        m_signature = signature;
      }
      return signature;
    }
    return m_signature;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getSignature();
  }

  protected String[] getStringArray(
      String @Nullable [] methodArray, String @Nullable [] classArray) {
    if (isEmpty(methodArray) && isEmpty(classArray)) {
      // The common case. Bail out before allocating the set and the result array.
      return EMPTY_STRING_ARRAY;
    }
    final Set<String> vResult = new HashSet<>();
    if (null != methodArray) {
      Collections.addAll(vResult, methodArray);
    }
    if (null != classArray) {
      Collections.addAll(vResult, classArray);
    }
    return vResult.toArray(EMPTY_STRING_ARRAY);
  }

  private static boolean isEmpty(String @Nullable [] array) {
    return array == null || array.length == 0;
  }

  protected void setGroups(String[] groups) {
    m_groups = groups;
  }

  protected void setGroupsDependedUpon(String[] groups, Collection<String> xmlGroupDependencies) {
    if (isEmpty(groups) && xmlGroupDependencies.isEmpty()) {
      m_groupsDependedUpon = EMPTY_STRING_ARRAY;
      return;
    }
    List<String> l = new ArrayList<>();
    l.addAll(Arrays.asList(groups));
    l.addAll(xmlGroupDependencies);
    m_groupsDependedUpon = l.toArray(EMPTY_STRING_ARRAY);
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
  public @Nullable String getMissingGroup() {
    return m_missingGroup;
  }

  /** {@inheritDoc} */
  @Override
  public void setMissingGroup(@Nullable String group) {
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
  public void setDescription(@Nullable String description) {
    m_description = description;
  }

  /** {@inheritDoc} */
  @Override
  public @Nullable String getDescription() {
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

  /**
   * @return {@code true} when this (cloned) method represents a single invocation of a parallel
   *     {@code invocationCount}, for which the firstTimeOnly/lastTimeOnly configuration methods are
   *     run around the thread pool rather than inside the invocation.
   */
  public boolean skipFirstAndLastTimeOnlyConfigs() {
    return m_skipFirstAndLastTimeOnlyConfigs;
  }

  public void setSkipFirstAndLastTimeOnlyConfigs(boolean skip) {
    m_skipFirstAndLastTimeOnlyConfigs = skip;
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
  public @Nullable IRetryAnalyzer getRetryAnalyzer(ITestResult result) {
    return getRetryAnalyzerConsideringMethodParameters(result);
  }

  @Override
  public void setRetryAnalyzerClass(Class<? extends IRetryAnalyzer> clazz) {
    m_retryAnalyzerClass = clazz == null ? DisabledRetryAnalyzer.class : clazz;
  }

  /**
   * @return the retry analyzer class, never null: it is {@link DisabledRetryAnalyzer} until a retry
   *     analyzer is set, and the setter normalises null back to it.
   */
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
    Collection<Integer> failed = failedInvocations();
    return failed == null ? new ArrayList<>() : new ArrayList<>(failed);
  }

  @Override
  public void addFailedInvocationNumber(int number) {
    Collection<Integer> failed = failedInvocations();
    if (failed == null) {
      failed = new ConcurrentLinkedQueue<>();
      // Losing the race means another thread already installed a queue; record into theirs, or the
      // failure numbers would be split across two queues and one of them thrown away.
      if (!FAILED_INVOCATIONS.compareAndSet(this, null, failed)) {
        // The winner's queue is never replaced, so the re-read hands back that one.
        failed = Objects.requireNonNull(failedInvocations());
      }
    }
    failed.add(number);
  }

  @SuppressWarnings("unchecked")
  private @Nullable Collection<Integer> failedInvocations() {
    return m_failedInvocationNumbers;
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
  public @Nullable XmlTest getXmlTest() {
    return m_xmlTest;
  }

  public void setXmlTest(@Nullable XmlTest xmlTest) {
    m_xmlTest = xmlTest;
  }

  /**
   * Binds this method to the {@code <class>} and {@code <include>} tags it was scheduled for.
   *
   * <p>Only test methods are bound. A configuration method still resolves its parameters by name,
   * through {@link XmlTestUtils}, which cannot tell two repeats of a tag apart.
   *
   * @param xmlClass - the {@code <class>} occurrence, null when no tag named this method.
   * @param xmlInclude - the {@code <include>} occurrence inside it, null when none names it.
   * @param occurrenceIndex - which occurrence this is, counting from zero within this method and
   *     instance.
   */
  public void setXmlOccurrence(
      @Nullable XmlClass xmlClass, @Nullable XmlInclude xmlInclude, int occurrenceIndex) {
    m_xmlClass = xmlClass;
    m_xmlInclude = xmlInclude;
    m_xmlOccurrenceIndex = occurrenceIndex;
  }

  @Nullable
  XmlClass getXmlClass() {
    return m_xmlClass;
  }

  @Nullable
  XmlInclude getXmlInclude() {
    return m_xmlInclude;
  }

  int getXmlOccurrenceIndex() {
    return m_xmlOccurrenceIndex;
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
    // The bound tags are the ones that were scheduled, which is the only thing that tells two
    // repeats of them apart. Read downwards from the <class>, whose getAllParameters walks up to
    // the <test> and the <suite>, then overlay the <include>'s own: an XmlInclude may be shared
    // between two XmlClass occurrences -- XmlClass.clone() hands its list straight over -- so its
    // parent pointer cannot say which occurrence is asking, and only the local parameters can be
    // read from it.
    XmlClass xmlClass = m_xmlClass;
    XmlInclude xmlInclude = m_xmlInclude;
    if (xmlClass != null) {
      Map<String, String> result = xmlClass.getAllParameters();
      if (xmlInclude != null) {
        result.putAll(xmlInclude.getLocalParameters());
      }
      return result;
    }
    if (xmlInclude != null) {
      return xmlInclude.getAllParameters();
    }
    // No test class bound yet means no <class> tag can match, which XmlTestUtils answers with
    // the suite and <test> parameters on their own.
    ITestClass testClass = getTestClass();
    return XmlTestUtils.findMethodParameters(
        test, testClass == null ? null : testClass.getName(), getMethodName());
  }

  @Override
  public String getQualifiedName() {
    return getRealClass().getName() + "." + getMethodName();
  }

  @Override
  @Deprecated
  public @Nullable IParameterInfo getFactoryMethodParamsInfo() {
    return getFactoryParameterInfo();
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

  private @Nullable IRetryAnalyzer getRetryAnalyzerConsideringMethodParameters(ITestResult tr) {
    if (this.m_retryAnalyzerClass.equals(DisabledRetryAnalyzer.class)) {
      return null;
    }
    if (isNotParameterisedTest(tr)) {
      this.m_retryAnalyzer = computeRetryAnalyzerInstanceToUse(tr);
      return this.m_retryAnalyzer;
    }

    final String keyAsString = getSimpleName() + "#" + parameterId(tr);
    return retryAnalyzers()
        .computeIfAbsent(
            keyAsString,
            key -> {
              BasicAttributes ba = new BasicAttributes(null, this.m_retryAnalyzerClass);
              CreationAttributes attributes = new CreationAttributes(tr.getTestContext(), ba, null);
              return (IRetryAnalyzer) Dispenser.newInstance(m_objectFactory).dispense(attributes);
            });
  }

  /**
   * @return the per-parameter retry analyzer cache, creating it on the first call. The CAS matters:
   *     two threads settling on different maps would each build their own analyzer for the same
   *     key, and a retry analyzer that loses its count lets a test retry more often than it should.
   */
  @SuppressWarnings("unchecked")
  private ConcurrentHashMap<String, IRetryAnalyzer> retryAnalyzers() {
    ConcurrentHashMap<String, IRetryAnalyzer> analyzers = m_testMethodToRetryAnalyzer;
    if (analyzers == null) {
      analyzers = new ConcurrentHashMap<>();
      if (!RETRY_ANALYZERS.compareAndSet(this, null, analyzers)) {
        // As above: the map the winning thread installed stays put.
        analyzers = Objects.requireNonNull(m_testMethodToRetryAnalyzer);
      }
    }
    return analyzers;
  }

  private static String parameterId(ITestResult itr) {
    return Integer.toString(itr.getParameterIndex());
  }

  private static boolean isNotParameterisedTest(ITestResult tr) {
    return Optional.ofNullable(tr.getParameters()).orElse(new Object[0]).length == 0;
  }

  private @Nullable IRetryAnalyzer computeRetryAnalyzerInstanceToUse(ITestResult tr) {
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
