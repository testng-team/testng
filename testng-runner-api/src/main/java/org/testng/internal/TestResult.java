package org.testng.internal;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.IAttributes;
import org.testng.IClass;
import org.testng.IFactoryInstance;
import org.testng.ITest;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Objects;

/** This class represents the result of a test. */
public class TestResult implements ITestResult {

  private static final Object[] NO_FACTORY_PARAMETERS = {};

  private final ITestNGMethod m_method;
  private List<ITestNGMethod> skippedDueTo = new ArrayList<>();
  private boolean skipAnalysed = false;
  private int m_status = CREATED;
  private @Nullable Throwable m_throwable = null;
  private long m_startMillis = 0;
  private long m_endMillis = 0;
  private String m_name;
  private @Nullable String m_host;
  private Object[] m_parameters = {};
  private String m_instanceName;
  private @Nullable ITestContext m_context;
  private int m_parameterIndex = -1;
  private boolean m_wasRetried;
  private final IAttributes m_attributes = new Attributes();
  private final String id = UUID.randomUUID().toString();

  /**
   * @param method The method this result reports on.
   * @param liteWeightInMemoryFriendlyMode Whether the method may be snapshotted into a {@link
   *     LiteWeightTestNGMethod} when memory friendly mode is on. A reported result holds the
   *     snapshot; the carrier {@link #newTestResult(ITestNGMethod, Object[], int)} builds holds the
   *     live method, because a configuration method that declares an {@link ITestResult} parameter
   *     is handed that carrier and mutates the method through it.
   */
  private TestResult(ITestNGMethod method, boolean liteWeightInMemoryFriendlyMode) {
    ITestClass boundClass = Utils.requireTestClassOf(method);
    m_method =
        liteWeightInMemoryFriendlyMode && RuntimeBehavior.isMemoryFriendlyMode()
            ? new LiteWeightTestNGMethod(method)
            : method;
    m_instanceName = boundClass.getName();
    m_name = computeName(method, boundClass);
  }

  /**
   * The parameter carrier the invoker builds before it starts reporting: it knows the method and
   * the arguments, and nothing about the outcome. Its status stays {@link ITestResult#CREATED}, its
   * millis stay zero and it carries no context until {@link #newTestResultFrom} replaces it.
   */
  public static TestResult newTestResult(ITestNGMethod method, Object[] parameters, int index) {
    TestResult result = new TestResult(method, false);
    result.setParameters(parameters);
    result.initParameterIndex(index);
    return result;
  }

  public static TestResult newTestResultFor(ITestNGMethod method) {
    return newContextAwareTestResult(method, null);
  }

  public static TestResult newContextAwareTestResult(
      ITestNGMethod method, @Nullable ITestContext ctx) {
    TestResult result = new TestResult(method, true);
    long time = System.currentTimeMillis();
    result.init(ctx, null, time, 0L);
    return result;
  }

  public static TestResult newTestResultWithCauseAs(
      ITestNGMethod method, @Nullable ITestContext ctx, Throwable t) {
    TestResult result = new TestResult(method, true);
    long time = System.currentTimeMillis();
    result.init(ctx, t, time, time);
    return result;
  }

  public static TestResult newEndTimeAwareTestResult(
      ITestNGMethod method, @Nullable ITestContext ctx, @Nullable Throwable t, long start) {
    TestResult result = new TestResult(method, true);
    long time = System.currentTimeMillis();
    result.init(ctx, t, start, time);
    return result;
  }

  public static TestResult newTestResultFrom(
      TestResult result, ITestNGMethod method, @Nullable ITestContext ctx, long start) {
    TestResult testResult = new TestResult(method, true);
    testResult.setParameters(result.getParameters());
    testResult.initParameterIndex(result.getParameterIndex());
    testResult.setHost(result.getHost());
    testResult.init(ctx, null, start, 0L);
    TestResult.copyAttributes(result, testResult);
    return testResult;
  }

  private void init(@Nullable ITestContext ctx, @Nullable Throwable t, long start, long end) {
    m_throwable = t;
    if (null == m_throwable) {
      m_status = ITestResult.SUCCESS;
    }
    m_startMillis = start;
    m_endMillis = end;
    m_context = ctx;
  }

  /**
   * The name of a result: either the method name, {@link ITest#getTestName()} or the instance's
   * {@code toString()} if it has been overridden. Refines {@link #m_instanceName} in that last
   * case, which is why it is called from the constructor rather than folded into it.
   */
  private String computeName(ITestNGMethod method, ITestClass boundClass) {
    Object instance = method.getInstance();

    if (instance == null) {
      return method.getMethodName();
    }
    if (instance instanceof ITest) {
      String testName = ((ITest) instance).getTestName();
      if (testName != null) {
        return testName;
      }
      if (Utils.getVerbose() > 1) {
        String msg =
            String.format(
                "Warning: [%s] implementation on class [%s] returned null. Defaulting to method name",
                ITest.class.getName(), instance.getClass().getName());
        System.err.println(msg);
      }
      return method.getMethodName();
    }
    String boundName = boundClass.getTestName();
    if (boundName != null) {
      return boundName;
    }
    // Only display toString() if it's been overridden by the user. Ask the declaring class rather
    // than comparing two Method objects: Method.equals compares exactly that, plus a name and a
    // signature that are fixed here, and this way the instance is never stringified for nothing.
    try {
      if (instance.getClass().getMethod("toString").getDeclaringClass() != Object.class) {
        String string = instance.toString();
        m_instanceName = string.startsWith("class ") ? string.substring("class ".length()) : string;
        return method.getMethodName() + " on " + m_instanceName;
      }
    } catch (NoSuchMethodException ignore) {
      // ignore
    }
    return method.getMethodName();
  }

  @Override
  public void setEndMillis(long millis) {
    m_endMillis = millis;
  }

  /**
   * If this result's related instance implements ITest or use @Test(testName=...), returns its test
   * name, otherwise returns null.
   */
  @Override
  public @Nullable String getTestName() {
    Object instance = this.m_method.getInstance();
    if (instance instanceof ITest) {
      return ((ITest) instance).getTestName();
    }
    return Utils.requireTestClassOf(m_method).getTestName();
  }

  @Override
  public String getName() {
    return m_name;
  }

  /** @return Returns the method. */
  @Override
  public ITestNGMethod getMethod() {
    return m_method;
  }

  /** @return Returns the status. */
  @Override
  public int getStatus() {
    return m_status;
  }

  /** @param status The status to set. */
  @Override
  public void setStatus(int status) {
    m_status = status;
  }

  @Override
  public boolean isSuccess() {
    return ITestResult.SUCCESS == m_status;
  }

  /** @return Returns the testClass. */
  @Override
  public IClass getTestClass() {
    return Utils.requireTestClassOf(m_method);
  }

  /** @return Returns the throwable. */
  @Override
  public @Nullable Throwable getThrowable() {
    return m_throwable;
  }

  /** @param throwable The throwable to set. */
  @Override
  public void setThrowable(@Nullable Throwable throwable) {
    m_throwable = throwable;
  }

  /** @return Returns the endMillis. */
  @Override
  public long getEndMillis() {
    return m_endMillis;
  }

  /** @return Returns the startMillis. */
  @Override
  public long getStartMillis() {
    return m_startMillis;
  }

  @Override
  public String toString() {
    List<String> output = Reporter.getOutput(this);
    return Objects.toStringHelper(getClass())
        .omitNulls()
        .omitEmptyStrings()
        .add("name", getName())
        .add("status", toString(m_status))
        .add("method", m_method)
        .add("output", !output.isEmpty() ? output.get(0) : null)
        .toString();
  }

  private static String toString(int status) {
    switch (status) {
      case SUCCESS:
        return "SUCCESS";
      case FAILURE:
        return "FAILURE";
      case SKIP:
        return "SKIP";
      case SUCCESS_PERCENTAGE_FAILURE:
        return "SUCCESS WITHIN PERCENTAGE";
      case STARTED:
        return "STARTED";
      case CREATED:
        return "CREATED";
      default:
        throw new TestNGException("Encountered an un-defined test status of [" + status + "].");
    }
  }

  @Override
  public @Nullable String getHost() {
    return m_host;
  }

  public void setHost(@Nullable String host) {
    m_host = host;
  }

  @Override
  public Object[] getParameters() {
    return m_parameters;
  }

  @Override
  public void setParameters(Object[] parameters) {
    m_parameters = LegacyParameterSnapshotter.snapshot(parameters);
  }

  @Override
  public @Nullable Object getInstance() {
    Object instance = m_method.getInstance();
    return instance == null ? null : IParameterInfo.embeddedInstance(instance);
  }

  @Override
  public Object[] getFactoryParameters() {
    return m_method
        .getFactoryInstance()
        .map(IFactoryInstance::getParameters)
        .orElse(NO_FACTORY_PARAMETERS);
  }

  @Override
  public @Nullable Object getAttribute(String name) {
    return m_attributes.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    m_attributes.setAttribute(name, value);
  }

  @Override
  public Set<String> getAttributeNames() {
    return m_attributes.getAttributeNames();
  }

  @Override
  public @Nullable Object removeAttribute(String name) {
    return m_attributes.removeAttribute(name);
  }

  @Override
  public @Nullable ITestContext getTestContext() {
    return m_context;
  }

  public void setContext(@Nullable ITestContext context) {
    m_context = context;
  }

  @Override
  public int compareTo(ITestResult comparison) {
    return Long.compare(getStartMillis(), comparison.getStartMillis());
  }

  @Override
  public String getInstanceName() {
    return m_instanceName;
  }

  @Override
  public void setTestName(String name) {
    m_name = requireNonNull(name, "a test result carries a name");
  }

  private void initParameterIndex(int index) {
    this.m_parameterIndex = index;
  }

  /** @deprecated This method is a no-op and will be removed in a future release. */
  @Deprecated(forRemoval = true)
  public void setParameterIndex(int parameterIndex) {}

  @Override
  public int getParameterIndex() {
    return m_parameterIndex;
  }

  @Override
  public boolean wasRetried() {
    return m_wasRetried;
  }

  @Override
  public void setWasRetried(boolean wasRetried) {
    this.m_wasRetried = wasRetried;
  }

  @Override
  public List<ITestNGMethod> getSkipCausedBy() {
    if (this.m_status != SKIP || skipAnalysed) {
      return Collections.unmodifiableList(skippedDueTo);
    }
    skipAnalysed = true;
    ITestContext context = m_context;
    if (context == null) {
      return Collections.unmodifiableList(skippedDueTo);
    }
    // check if there were any config failures
    Set<ITestResult> skippedConfigs = context.getFailedConfigurations().getAllResults();
    for (ITestResult skippedConfig : skippedConfigs) {
      if (isGlobalFailure(skippedConfig) || isRelated(skippedConfig)) {
        // If there's a failure in @BeforeTest/@BeforeSuite/@BeforeClass
        // then the reason is most often just one method.
        skippedDueTo.add(skippedConfig.getMethod());
      }
      if (belongToSameGroup(skippedConfig)) {
        // If its @BeforeGroups then there's a chance that there could be more than one
        // method. So let's add everything.
        skippedDueTo.add(skippedConfig.getMethod());
      }
    }
    if (!skippedDueTo.isEmpty()) {
      // If we found at least one skipped due to reason, then it's time to return back.
      return Collections.unmodifiableList(skippedDueTo);
    }
    // Looks like we didn't have any configuration failures. So some upstream method perhaps failed.
    ITestNGMethod skippedMethod = m_method;
    if (skippedMethod.getMethodsDependedUpon().length == 0) {
      // Maybe group dependencies exist ?
      if (skippedMethod.getGroupsDependedUpon().length == 0) {
        return Collections.emptyList();
      }
      List<String> upstreamGroups = Arrays.asList(skippedMethod.getGroupsDependedUpon());
      List<ITestResult> allFailures =
          Lists.merge(
              context.getFailedTests().getAllResults(),
              context.getFailedButWithinSuccessPercentageTests().getAllResults());
      skippedDueTo =
          allFailures.stream()
              .map(ITestResult::getMethod)
              .filter(
                  method -> {
                    List<String> currentMethodGroups = Arrays.asList(method.getGroups());
                    List<String> intersection =
                        Lists.intersection(upstreamGroups, currentMethodGroups);
                    return !intersection.isEmpty();
                  })
              .collect(Collectors.toList());

      return Collections.unmodifiableList(skippedDueTo);
    }
    List<String> upstreamMethods = Arrays.asList(skippedMethod.getMethodsDependedUpon());

    // So we have dependsOnMethod failures
    List<ITestResult> allFailures =
        Lists.merge(
            context.getFailedTests().getAllResults(),
            context.getFailedButWithinSuccessPercentageTests().getAllResults());
    skippedDueTo =
        allFailures.stream()
            .map(ITestResult::getMethod)
            .filter(method -> matches(upstreamMethods, method))
            .collect(Collectors.toList());
    return Collections.unmodifiableList(skippedDueTo);
  }

  private static boolean matches(List<String> upstreamMethods, ITestNGMethod method) {
    if (upstreamMethods.contains(method.getQualifiedName())
        || upstreamMethods.contains(method.getMethodName())) {
      return true;
    }
    return upstreamMethods.stream()
        .map(Pattern::compile)
        .anyMatch(
            each ->
                each.matcher(method.getQualifiedName()).matches()
                    || each.matcher(method.getMethodName()).matches());
  }

  @Override
  public String id() {
    return id;
  }

  private static boolean isGlobalFailure(ITestResult result) {
    ITestNGMethod m = result.getMethod();
    return m.isBeforeTestConfiguration() || m.isBeforeSuiteConfiguration();
  }

  private boolean isRelated(ITestResult result) {
    ITestNGMethod m = result.getMethod();
    if (!m.isBeforeClassConfiguration() && !m.isBeforeMethodConfiguration()) {
      return false;
    }
    Object current = this.getInstance();
    Object thatObject = result.getInstance();
    if (current == null || thatObject == null) {
      return false;
    }
    return current.getClass().isAssignableFrom(thatObject.getClass())
        || thatObject.getClass().isAssignableFrom(current.getClass());
  }

  private boolean belongToSameGroup(ITestResult result) {
    ITestNGMethod m = result.getMethod();
    if (!m.isBeforeGroupsConfiguration()) {
      return false;
    }
    String[] myGroups = m_method.getGroups();
    if (myGroups.length == 0 || m.getGroups().length == 0) {
      return false;
    }

    List<String> cfgMethodGroups = Arrays.asList(m.getGroups());
    return Arrays.stream(myGroups).anyMatch(cfgMethodGroups::contains);
  }

  public static void copyAttributes(ITestResult source, ITestResult target) {
    source
        .getAttributeNames()
        .forEach(
            name -> {
              Object value = source.getAttribute(name);
              if (value != null) {
                target.setAttribute(name, value);
              }
            });
  }
}
