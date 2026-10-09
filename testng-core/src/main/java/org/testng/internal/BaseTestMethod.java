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
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * The base class of the methods that TestNG runs. Its subclasses stand for test methods,
 * configuration methods and {@code @Factory} methods.
 */
public abstract class BaseTestMethod
    implements ITestNGMethod, IInvocationStatus, IInstanceIdentity {

  private static final Pattern SPACE_SEPARATOR_PATTERN = Pattern.compile(" +");

  /**
   * An empty array that every method shares for its empty group arrays. Most methods have empty
   * group arrays. A field initializer such as {@code String[] x = {}} creates a new array for each
   * object. A large &#64;Factory suite creates one method object for each instance, and each method
   * object has several group arrays. So new empty arrays would add up. Nobody can change an empty
   * array, so all the methods can share this one, even though the getters return it as it is.
   */
  static final String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * The test class where TestNG found this method. It can be a subclass of the class that declares
   * the method.
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
  // A group that this method depends on, but that no method belongs to.
  private @Nullable String m_missingGroup;
  private @Nullable String m_description = null;
  protected AtomicInteger m_currentInvocationCount = new AtomicInteger(0);
  private int m_parameterInvocationCount = 1;
  // True for the copies that TestNG makes of a method, one for each invocation, when its
  // invocationCount runs in parallel (threadPoolSize > 1). TestNG runs the firstTimeOnly
  // @BeforeMethod once before the whole pool, and the lastTimeOnly @AfterMethod once after it. So
  // the copies must not run them.
  private boolean m_skipFirstAndLastTimeOnlyConfigs;
  private volatile boolean m_emptyDataProviderSeen;
  private @Nullable Callable<Boolean> m_moreInvocationChecker;
  private @Nullable IRetryAnalyzer m_retryAnalyzer = null;
  private Class<? extends IRetryAnalyzer> m_retryAnalyzerClass = DisabledRetryAnalyzer.class;
  private boolean m_skipFailedInvocations = true;
  private long m_invocationTimeOut = 0L;

  // Has values only when an <include invocation-numbers="..."> names this method. It starts as the
  // shared empty list, not as a new list for each method. setInvocationNumbers() replaces the whole
  // list, and TestNG does not change the list through this field. So the methods can share one
  // empty list that nobody can change.
  private List<Integer> m_invocationNumbers = Collections.emptyList();
  // Null until the method has dependencies, and most methods have none. An empty HashSet still
  // costs the set and the HashMap inside it. A large @Factory suite would hold two of them for each
  // method of each instance. The volatile write in the setters makes the value visible to the
  // worker threads.
  private volatile @Nullable Set<ITestNGMethod> downstreamDependencies;
  private volatile @Nullable Set<ITestNGMethod> upstreamDependencies;
  // Set only when an invocation fails. An empty ConcurrentLinkedQueue still costs the queue and its
  // first empty node. So create it only when there is a failure to record.
  @SuppressWarnings("rawtypes")
  private volatile @Nullable Collection m_failedInvocationNumbers;

  // The @Nullable on the value type lets compareAndSet() in addFailedInvocationNumber() pass null
  // as the value that it expects to replace. The package is @NullMarked, so without it the value
  // type is non-null, and NullAway rejects the call.
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
  // The <class> and <include> tags that scheduled this method. A suite can repeat both tags, and
  // each copy has its own parameters. So findMethodParameters() reads the tag object, not the tag
  // name. The field for the <include> is null when no <include> names the method, for example in a
  // <class> without <methods>. The field for the <class> is null when no <class> tag names the
  // class, for example a class that only a @Factory creates.
  private @Nullable XmlClass m_xmlClass;
  private @Nullable XmlInclude m_xmlInclude;
  // Which copy of those tags this is, counted for this method and instance. equals() and
  // hashCode() use it, so that two copies of the same tag stay separate nodes in the method graph.
  // XmlClass#getIndex and XmlInclude#getIndex cannot do this, because many suites leave them at
  // zero. For example, a suite from the Java API leaves both at zero, and a YAML suite leaves the
  // index of each <include> at zero.
  private int m_xmlOccurrenceIndex;
  private final IObject.@Nullable IdentifiableObject m_instance;

  // Set only for a test with parameters and a retry analyzer, so it stays null for almost every
  // method. retryAnalyzers() sets it with a compare-and-set through RETRY_ANALYZERS, not under a
  // lock. A lock object for each method would cost a quarter of what leaving out the map saves.
  @SuppressWarnings("rawtypes")
  private volatile @Nullable ConcurrentHashMap m_testMethodToRetryAnalyzer;

  // The value type is @Nullable for the same reason as in FAILED_INVOCATIONS.
  @SuppressWarnings("rawtypes")
  private static final AtomicReferenceFieldUpdater<BaseTestMethod, @Nullable ConcurrentHashMap>
      RETRY_ANALYZERS =
          AtomicReferenceFieldUpdater.newUpdater(
              BaseTestMethod.class, ConcurrentHashMap.class, "m_testMethodToRetryAnalyzer");

  protected final ITestObjectFactory m_objectFactory;

  /**
   * Creates a method for {@code com}.
   *
   * @param objectFactory the factory that creates the objects that this method needs, for example
   *     its retry analyzers.
   * @param methodName the name of the method.
   * @param com the Java method or constructor.
   * @param annotationFinder the finder that reads the annotations of the method.
   * @param instance the test instance of the method, or {@code null} when it has none.
   */
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

  /**
   * Sets the value that {@link #isAlwaysRun()} returns.
   *
   * @param alwaysRun the {@code alwaysRun} value of the annotation.
   */
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
    // TestNG calls this method often, for example through TestNgMethodUtils.isSameInstance(). So
    // use plain null checks. A chain of Optional calls would create three Optional objects on each
    // call.
    if (m_instance == null) {
      return null;
    }
    return IParameterInfo.embeddedInstance(m_instance.getInstance());
  }

  /**
   * Tells if the test instance of this method exists.
   *
   * <p>The answer is {@code false} only for a lazy {@code @Factory} instance that TestNG has not
   * created yet. Callers check this so that they do not create a lazy instance too early, for
   * example to build a message. This method never creates the instance.
   *
   * @return {@code false} when this method has a lazy {@code @Factory} instance that does not exist
   *     yet, otherwise {@code true}.
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
   * Returns the instance wrapper for a copy of this method. The wrapper holds the same test
   * instance and the same instance id.
   *
   * <p>This method reads the test instance. So it creates a lazy {@code @Factory} instance that
   * does not exist yet. The wrapper does not keep the {@code @Factory} data of the instance, so
   * {@link #getFactoryParameterInfo()} of the copy returns {@code null} (GITHUB-3576).
   *
   * @return the new wrapper, or {@code null} when this method has no test instance.
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
   * @return the groups of this method, together with the groups of its class.
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

  /**
   * {@inheritDoc}
   *
   * <p>The set does not follow later changes. {@link #setDownstreamDependencies(Set)} puts in a new
   * set, and does not refill the old one. So a set that you got before a change keeps the old
   * methods. TestNG itself does not keep one of these sets across a change. A method without
   * dependencies holds no set at all, so that it does not carry an empty set.
   */
  @Override
  public Set<ITestNGMethod> downstreamDependencies() {
    return readOnlyView(downstreamDependencies);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The set does not follow later changes, as {@link #downstreamDependencies()} explains.
   */
  @Override
  public Set<ITestNGMethod> upstreamDependencies() {
    return readOnlyView(upstreamDependencies);
  }

  /**
   * Replaces the downstream dependencies, which are the methods that depend on this method. A set
   * that {@link #downstreamDependencies()} returned before keeps the old methods.
   *
   * @param methods the methods that depend on this method.
   */
  public void setDownstreamDependencies(Set<ITestNGMethod> methods) {
    downstreamDependencies = setupDependencies(methods);
  }

  /**
   * Replaces the upstream dependencies, which are the methods that this method depends on. A set
   * that {@link #upstreamDependencies()} returned before keeps the old methods.
   *
   * @param methods the methods that this method depends on.
   */
  public void setUpstreamDependencies(Set<ITestNGMethod> methods) {
    upstreamDependencies = setupDependencies(methods);
  }

  private static Set<ITestNGMethod> readOnlyView(@Nullable Set<ITestNGMethod> dependencies) {
    return dependencies == null
        ? Collections.emptySet()
        : Collections.unmodifiableSet(dependencies);
  }

  /**
   * Copies {@code methods} into a new set. In memory-friendly mode, the set holds light copies of
   * the methods.
   *
   * @return the new set, or {@code null} when {@code methods} is empty. So a method without
   *     dependencies holds no set at all.
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
    if (m_timeOut != 0) {
      return m_timeOut;
    }
    if (m_xmlTest == null) {
      return 0;
    }
    if (isBoundedAtTestScope(m_xmlTest)) {
      return 0;
    }
    return m_xmlTest.getTimeOut(0);
  }

  /**
   * Tells if TestNG already applies the XML time-out to the whole {@code <test>}. In that case, the
   * method must not get the same time-out a second time.
   *
   * <p>The DTD gives the time-out of {@code <suite>} and {@code <test>} a meaning for each parallel
   * mode. With {@code parallel="methods"}, it stops a method. With {@code parallel="tests"}, it
   * stops a whole {@code <test>}. Only {@code SuiteRunner.runInParallelTestMode()} stops a whole
   * {@code <test>}. It gives each {@code <test>} worker the time-out of the <em>suite</em>. The
   * parallel mode of the suite selects that code. {@code TestTaskExecutor} would apply the time-out
   * of the {@code <test>} itself, but that mode does not use it, because {@code ParallelMode.TESTS}
   * is not parallel inside a {@code <test>}.
   *
   * <p>So the method does not get the time-out when both of these are true:
   *
   * <ul>
   *   <li>The suite has {@code parallel="tests"}.
   *   <li>The {@code <test>} has no time-out of its own, or the same time-out as the suite.
   * </ul>
   *
   * <p>In the other cases, nothing else applies the time-out, so the method keeps it. Two examples
   * are a time-out only on the {@code <test>}, and {@code parallel="tests"} only on the {@code
   * <test>}. If the method did not keep it, TestNG would drop the time-out with no warning.
   *
   * <p>Without this check, each method of the {@code <test>} would get the time-out of the suite. A
   * method with a time-out runs on a thread of its own.
   */
  private static boolean isBoundedAtTestScope(XmlTest xmlTest) {
    XmlSuite suite = xmlTest.getSuite();
    return suite.getParallel() == XmlSuite.ParallelMode.TESTS
        && Objects.equals(xmlTest.getTimeOut(), suite.getTimeOut());
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
   * Returns what TestNG knows about the {@code @Factory} call that created the test instance of
   * this method.
   *
   * <p>{@link ITestNGMethod} does not have this method, unlike the deprecated {@link
   * #getFactoryMethodParamsInfo()}. So TestNG can use the details of lazy instances without making
   * them public.
   *
   * <p>Note: a copy that {@code clone()} makes returns {@code null}, even when a {@code @Factory}
   * created the instance. So the results of a parallel {@code invocationCount} have no factory
   * parameters (GITHUB-3576).
   *
   * @return the factory data, or {@code null} when no {@code @Factory} created the test instance.
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

  /** Does nothing. */
  @Override
  public void setInvocationCount(int counter) {}

  /** {@inheritDoc} This class returns 100, the default value. */
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
   * @return the date.
   */
  @Override
  public long getDate() {
    return m_date;
  }

  /**
   * {@inheritDoc}
   *
   * @param date the date to set.
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
   * {@inheritDoc}
   *
   * <p>Two methods are equal when all of these match:
   *
   * <ul>
   *   <li>the class of the method object, for example {@link TestNGMethod}.
   *   <li>the Java class of the test class, and the id of the test instance. When neither method
   *       has a test class yet, only that fact must match.
   *   <li>the copy of the XML tags that scheduled the method.
   *   <li>the Java method or constructor.
   * </ul>
   */
  @Override
  // getClass() on purpose. ConfigurationMethod, FactoryMethod and TestNGMethod do not override
  // equals(). When two of them wrap the same method, the same class and the same instance id, only
  // this check tells them apart. Many HashSet and HashMap objects hold them. No test fails if this
  // check uses instanceof instead, so this is a choice, not a fix. The class cannot be sealed
  // either. Those three classes extend it, and the bundle exports org.testng.internal
  // (Export-Package).
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
                // Compare the instance ids, not the instances. TestNG compares methods often while
                // it builds the method graph, and comparing the instances would create the lazy
                // @Factory instances.
                && Objects.equals(getInstanceId(), other.getInstanceId());

    return isEqual
        && m_xmlOccurrenceIndex == other.m_xmlOccurrenceIndex
        && getConstructorOrMethod().equals(other.getConstructorOrMethod());
  }

  /**
   * {@inheritDoc}
   *
   * <p>The hash code uses the Java method, the instance id, and the copy of the XML tags.
   */
  @Override
  public int hashCode() {
    int hash = m_method.hashCode();
    // Use the instance id, not the hash code of the instance itself. So a method in a hash-based
    // collection never creates its lazy @Factory instance.
    // Note: equals() skips the instance id when neither method has a test class. Two such methods
    // can then be equal and still have different hash codes.
    UUID instanceId = getInstanceId();
    if (instanceId != null) {
      hash = hash * 31 + instanceId.hashCode();
    }
    return hash * 31 + m_xmlOccurrenceIndex;
  }

  /**
   * Reads the groups of this method from its annotation, and from the same annotation on its class.
   * Then reads the groups and the methods that this method depends on.
   *
   * @param annotationClass the type of the annotation to read, for example {@code ITestAnnotation}.
   */
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
    // When the lazy @Factory instance does not exist yet, keep the declaring class. Only a
    // constructor factory makes lazy instances, and it creates exactly that class. So there is no
    // need to create the instance to read its class.
    ITestOrConfiguration classAnnotation =
        getAnnotationFinder().findAnnotation(clazz, annotationClass);

    setGroups(
        getStringArray(
            null != annotation ? annotation.getGroups() : null,
            null != classAnnotation ? classAnnotation.getGroups() : null));

    initRestOfGroupDependencies(annotationClass);
  }

  /**
   * Reads the groups of a {@code @BeforeGroups} or {@code @AfterGroups} method. Then reads the
   * groups and the methods that this method depends on.
   *
   * @param annotationClass the type of the annotation to read.
   * @param groups the groups that the method runs before or after. When this is empty, this method
   *     reads the {@code groups} of the annotation instead.
   */
  protected void initBeforeAfterGroups(
      Class<? extends ITestOrConfiguration> annotationClass, String[] groups) {
    String @Nullable [] groupsAtMethodLevel =
        calculateGroupsToUseConsideringValuesAndGroupValues(annotationClass, groups);
    // @BeforeGroups and @AfterGroups cannot go on a class, so there are no class groups.
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
    // Find the groups that this method depends on
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
    // Add the class name to each method name that has no dot
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

  /**
   * Returns the finder that reads the annotations of this method.
   *
   * @return the annotation finder.
   */
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
        // Do not create a lazy @Factory instance only to show it in the signature. The factory
        // parameters, which instanceParameters() adds, already identify the instance.
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

  /**
   * Returns the simple name of the declaring class and the name of the method, for example {@code
   * LoginTest.testLogin}.
   *
   * @return the short name of this method.
   */
  public String getSimpleName() {
    return m_method.getDeclaringClass().getSimpleName() + "." + m_method.getName();
  }

  private String instanceParameters() {
    return getFactoryInstance()
        .map(it -> ", instance params:" + Arrays.toString(it.getParameters()))
        .orElse("");
  }

  /**
   * Returns the signature of this method, for messages. It holds the class, the method, the types
   * of the parameters, the priority and the instance. It also holds the factory parameters and the
   * custom attributes, when the method has them.
   *
   * @return the signature.
   */
  protected String getSignature() {
    if (m_signature == null) {
      String signature = computeSignature();
      // Keep the signature only when the instance exists. A signature that TestNG makes before it
      // creates a lazy @Factory instance would be wrong after that.
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

  /**
   * Returns the values of both arrays together, without duplicates.
   *
   * @param methodArray the values from the annotation of the method, or {@code null}.
   * @param classArray the values from the annotation of the class, or {@code null}.
   * @return the values of both arrays, in no fixed order.
   */
  protected String[] getStringArray(
      String @Nullable [] methodArray, String @Nullable [] classArray) {
    if (isEmpty(methodArray) && isEmpty(classArray)) {
      // This is the usual case. Return before creating the set and the result array.
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

  /**
   * Sets the groups of this method.
   *
   * @param groups the groups.
   */
  protected void setGroups(String[] groups) {
    m_groups = groups;
  }

  /**
   * Sets the groups that this method depends on.
   *
   * @param groups the groups from the annotations.
   * @param xmlGroupDependencies the groups from the {@code <dependencies>} tag of the {@code
   *     <test>}.
   */
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

  /**
   * Sets the methods that this method depends on.
   *
   * @param methods the names of the methods.
   */
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

  /** Does nothing. */
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

  /**
   * Sets whether this method is enabled. TestNG does not run a disabled method.
   *
   * @param enabled {@code true} to enable this method.
   */
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
   * Tells if this method is a copy for one invocation of a parallel {@code invocationCount}.
   *
   * <p>For such a copy, TestNG runs the {@code firstTimeOnly} and {@code lastTimeOnly}
   * configuration methods around the thread pool, not inside the invocation.
   *
   * @return {@code true} for such a copy.
   */
  public boolean skipFirstAndLastTimeOnlyConfigs() {
    return m_skipFirstAndLastTimeOnlyConfigs;
  }

  /**
   * Marks this method as a copy for one invocation of a parallel {@code invocationCount}.
   *
   * @param skip {@code true} for such a copy.
   */
  public void setSkipFirstAndLastTimeOnlyConfigs(boolean skip) {
    m_skipFirstAndLastTimeOnlyConfigs = skip;
  }

  /**
   * Tells if this copy, for one invocation of a parallel {@code invocationCount}, found its data
   * provider empty.
   *
   * <p>The copies do not report the empty data provider. The thread pool reports one skipped result
   * for the method instead.
   *
   * @return {@code true} when this copy found its data provider empty.
   */
  public boolean emptyDataProviderSeen() {
    return m_emptyDataProviderSeen;
  }

  /**
   * Records whether this copy found its data provider empty.
   *
   * @param seen {@code true} when the data provider was empty.
   */
  public void setEmptyDataProviderSeen(boolean seen) {
    m_emptyDataProviderSeen = seen;
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
        // This should never happen.
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
   * {@inheritDoc}
   *
   * @return the retry analyzer class, never {@code null}. It is {@link DisabledRetryAnalyzer} until
   *     you set a class. The setter also turns {@code null} into {@link DisabledRetryAnalyzer}.
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

  /**
   * Sets the time-out for all the invocations of this method together.
   *
   * @param timeOut the time-out, in milliseconds.
   */
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
      // When the compare-and-set fails, another thread set a queue first. Use that queue. Otherwise
      // the numbers would go into two queues, and one of the queues would be lost.
      if (!FAILED_INVOCATIONS.compareAndSet(this, null, failed)) {
        // No code replaces the queue after it is set, so this read returns the queue of the other
        // thread.
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

  /**
   * Sets the {@code <test>} of this method.
   *
   * @param xmlTest the {@code <test>}, or {@code null}.
   */
  public void setXmlTest(@Nullable XmlTest xmlTest) {
    m_xmlTest = xmlTest;
  }

  /**
   * Links this method to the {@code <class>} and {@code <include>} tags that scheduled it.
   *
   * <p>TestNG links only test methods. A configuration method still finds its parameters by name,
   * through {@link XmlTestUtils}. That cannot tell two copies of a tag apart.
   *
   * @param xmlClass the copy of the {@code <class>} tag, or {@code null} when no tag names this
   *     method.
   * @param xmlInclude the copy of the {@code <include>} tag inside it, or {@code null} when no
   *     {@code <include>} names this method.
   * @param occurrenceIndex which copy this is, counted from zero for this method and instance.
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
    // Read the parameters from the linked tags. Only those tags tell two copies of a tag apart.
    // Start with the <class>. Its getAllParameters() also reads the <test> and the <suite>. Then
    // add the parameters of the <include> itself. Two XmlClass copies can share one XmlInclude,
    // because XmlClass.clone() passes on its list as it is. So the link from the <include> to its
    // parent cannot say which copy asks. Only the local parameters of the <include> are safe to
    // read.
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
    // Without a test class, no <class> tag can match. XmlTestUtils then returns only the
    // parameters of the suite and the <test>.
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
   * Returns the retry analyzers of this method, one for each parameter index. The first call
   * creates the map.
   *
   * <p>The first call uses a compare-and-set, because two threads must not end up with two
   * different maps. Each thread would then create its own analyzer for the same key. An analyzer
   * that loses its count lets a test retry more often than it should.
   *
   * @return the map from a key for the parameters to the retry analyzer.
   */
  @SuppressWarnings("unchecked")
  private ConcurrentHashMap<String, IRetryAnalyzer> retryAnalyzers() {
    ConcurrentHashMap<String, IRetryAnalyzer> analyzers = m_testMethodToRetryAnalyzer;
    if (analyzers == null) {
      analyzers = new ConcurrentHashMap<>();
      if (!RETRY_ANALYZERS.compareAndSet(this, null, analyzers)) {
        // As in addFailedInvocationNumber(), no code replaces the map after it is set.
        analyzers = Objects.requireNonNull(m_testMethodToRetryAnalyzer);
      }
    }
    return analyzers;
  }

  private static String parameterId(ITestResult itr) {
    return Integer.toString(itr.getParameterIndex());
  }

  private static boolean isNotParameterisedTest(ITestResult tr) {
    // Do not use Optional.orElse(new Object[0]) here. orElse() evaluates its argument on every
    // call, so it would create an empty array each time.
    Object[] parameters = tr.getParameters();
    return parameters == null || parameters.length == 0;
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
