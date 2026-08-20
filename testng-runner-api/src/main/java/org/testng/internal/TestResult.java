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

  private @Nullable ITestNGMethod m_method = null;
  private List<ITestNGMethod> skippedDueTo = new ArrayList<>();
  private boolean skipAnalysed = false;
  private int m_status = CREATED;
  private @Nullable Throwable m_throwable = null;
  private long m_startMillis = 0;
  private long m_endMillis = 0;
  private @Nullable String m_name = null;
  private @Nullable String m_host;
  private Object[] m_parameters = {};
  private @Nullable String m_instanceName;
  private @Nullable ITestContext m_context;
  private int m_parameterIndex = -1;
  private boolean m_wasRetried;
  private final IAttributes m_attributes = new Attributes();
  private final String id = UUID.randomUUID().toString();

  private TestResult() {
    // defeat instantiation. We have factory methods.
  }

  public static TestResult newEmptyTestResult() {
    return new TestResult();
  }

  public static TestResult newTestResult(Object[] parameters, int index) {
    TestResult result = newEmptyTestResult();
    result.setParameters(parameters);
    result.initParameterIndex(index);
    return result;
  }

  public static TestResult newTestResultFor(ITestNGMethod method) {
    return newContextAwareTestResult(method, null);
  }

  public static TestResult newContextAwareTestResult(
      ITestNGMethod method, @Nullable ITestContext ctx) {
    TestResult result = newEmptyTestResult();
    long time = System.currentTimeMillis();
    result.init(method, ctx, null, time, 0L);
    return result;
  }

  public static TestResult newTestResultWithCauseAs(
      ITestNGMethod method, @Nullable ITestContext ctx, Throwable t) {
    TestResult result = newEmptyTestResult();
    long time = System.currentTimeMillis();
    result.init(method, ctx, t, time, time);
    return result;
  }

  public static TestResult newEndTimeAwareTestResult(
      ITestNGMethod method, @Nullable ITestContext ctx, @Nullable Throwable t, long start) {
    TestResult result = newEmptyTestResult();
    long time = System.currentTimeMillis();
    result.init(method, ctx, t, start, time);
    return result;
  }

  public static TestResult newTestResultFrom(
      TestResult result, ITestNGMethod method, @Nullable ITestContext ctx, long start) {
    TestResult testResult =
        TestResult.newTestResult(result.getParameters(), result.getParameterIndex());
    testResult.setHost(result.getHost());
    testResult.init(method, ctx, null, start, 0L);
    TestResult.copyAttributes(result, testResult);
    return testResult;
  }

  private void init(
      ITestNGMethod method,
      @Nullable ITestContext ctx,
      @Nullable Throwable t,
      long start,
      long end) {
    m_throwable = t;
    ITestClass boundClass = Utils.requireTestClassOf(method);
    m_instanceName = boundClass.getName();
    if (null == m_throwable) {
      m_status = ITestResult.SUCCESS;
    }
    m_startMillis = start;
    m_endMillis = end;
    m_method = RuntimeBehavior.isMemoryFriendlyMode() ? new LiteWeightTestNGMethod(method) : method;
    m_context = ctx;

    Object instance = method.getInstance();

    // Calculate the name: either the method name, ITest#getTestName or
    // toString() if it's been overridden.
    if (instance == null) {
      m_name = method.getMethodName();
      return;
    }
    if (instance instanceof ITest) {
      m_name = ((ITest) instance).getTestName();
      if (m_name != null) {
        return;
      }
      m_name = method.getMethodName();
      if (Utils.getVerbose() > 1) {
        String msg =
            String.format(
                "Warning: [%s] implementation on class [%s] returned null. Defaulting to method name",
                ITest.class.getName(), instance.getClass().getName());
        System.err.println(msg);
      }
      return;
    }
    String boundName = Utils.requireTestClassOf(method).getTestName();
    if (boundName != null) {
      m_name = boundName;
      return;
    }
    String string = instance.toString();
    // Only display toString() if it's been overridden by the user
    m_name = method.getMethodName();
    try {
      if (!Object.class.getMethod("toString").equals(instance.getClass().getMethod("toString"))) {
        m_instanceName = string.startsWith("class ") ? string.substring("class ".length()) : string;
        m_name = m_name + " on " + m_instanceName;
      }
    } catch (NoSuchMethodException ignore) {
      // ignore
    }
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
    if (this.m_method == null) {
      return null;
    }
    Object instance = this.m_method.getInstance();
    if (instance instanceof ITest) {
      return ((ITest) instance).getTestName();
    }
    return Utils.requireTestClassOf(m_method).getTestName();
  }

  @Override
  public @Nullable String getName() {
    return m_name;
  }

  /** @return Returns the method. */
  @Override
  public @Nullable ITestNGMethod getMethod() {
    return m_method;
  }

  /**
   * The method this result belongs to, for the members that only make sense on a result built
   * through one of the method-aware factories. {@link #newTestResult(Object[], int)} deliberately
   * builds a carrier that has no method, and those members are not reachable on it.
   */
  private ITestNGMethod requireMethod() {
    return requireNonNull(
        m_method, "This TestResult carries parameters only; it has no test method");
  }

  /** @param method The method to set. */
  public void setMethod(@Nullable ITestNGMethod method) {
    m_method = method;
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
    return Utils.requireTestClassOf(requireMethod());
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
    Object instance = requireMethod().getInstance();
    return instance == null ? null : IParameterInfo.embeddedInstance(instance);
  }

  @Override
  public Object[] getFactoryParameters() {
    return requireMethod()
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
  public @Nullable String getInstanceName() {
    return m_instanceName;
  }

  @Override
  public void setTestName(@Nullable String name) {
    m_name = name;
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
    ITestNGMethod skippedMethod = requireMethod();
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
    ITestNGMethod m = Utils.requireMethodOf(result);
    return m.isBeforeTestConfiguration() || m.isBeforeSuiteConfiguration();
  }

  private boolean isRelated(ITestResult result) {
    ITestNGMethod m = Utils.requireMethodOf(result);
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
    ITestNGMethod m = Utils.requireMethodOf(result);
    if (!m.isBeforeGroupsConfiguration()) {
      return false;
    }
    String[] myGroups = requireMethod().getGroups();
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
