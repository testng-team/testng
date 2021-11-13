package org.testng.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.testng.IAttributes;
import org.testng.IClass;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.collections.Objects;

/** This class represents the result of a test. */
public class TestResult implements ITestResult {

  private ITestNGMethod m_method = null;
  private List<ITestNGMethod> skippedDueTo = Lists.newArrayList();
  private boolean skipAnalysed = false;
  private int m_status = CREATED;
  private Throwable m_throwable = null;
  private long m_startMillis = 0;
  private long m_endMillis = 0;
  private String m_name = null;
  private String m_host;
  private Object[] m_parameters = {};
  private String m_instanceName;
  private ITestContext m_context;
  private int parameterIndex;
  private boolean m_wasRetried;
  private final IAttributes m_attributes = new Attributes();
  private final String id = UUID.randomUUID().toString();

  private TestResult() {
    // defeat instantiation. We have factory methods.
  }

  public static TestResult newEmptyTestResult() {
    return new TestResult();
  }

  public static TestResult newTestResultFor(ITestNGMethod method) {
    return newContextAwareTestResult(method, null);
  }

  public static TestResult newContextAwareTestResult(ITestNGMethod method, ITestContext ctx) {
    TestResult result = newEmptyTestResult();
    long time = System.currentTimeMillis();
    result.init(method, ctx, null, time, 0L);
    return result;
  }

  public static TestResult newTestResultWithCauseAs(
      ITestNGMethod method, ITestContext ctx, Throwable t) {
    TestResult result = newEmptyTestResult();
    long time = System.currentTimeMillis();
    result.init(method, ctx, t, time, time);
    return result;
  }

  public static TestResult newEndTimeAwareTestResult(
      ITestNGMethod method, ITestContext ctx, Throwable t, long start) {
    TestResult result = newEmptyTestResult();
    long time = System.currentTimeMillis();
    result.init(method, ctx, t, start, time);
    return result;
  }

  public static TestResult newTestResultFrom(
      TestResult result, ITestNGMethod method, ITestContext ctx, long start) {
    TestResult testResult = newEmptyTestResult();
    testResult.setHost(result.getHost());
    testResult.setParameters(result.getParameters());
    testResult.setParameterIndex(result.getParameterIndex());
    testResult.init(method, ctx, null, start, 0L);
    TestResult.copyAttributes(result, testResult);
    return testResult;
  }

  private void init(ITestNGMethod method, ITestContext ctx, Throwable t, long start, long end) {
    m_throwable = t;
    m_instanceName = method.getTestClass().getName();
    if (null == m_throwable) {
      m_status = ITestResult.SUCCESS;
    }
    m_startMillis = start;
    m_endMillis = end;
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      m_method = new LiteWeightTestNGMethod(method);
    } else {
      m_method = method;
    }
    m_context = ctx;

    Object instance = method.getInstance();

    // Calculate the name: either the method name, ITest#getTestName or
    // toString() if it's been overridden.
    if (instance == null) {
      m_name = m_method.getMethodName();
      return;
    }
    if (instance instanceof ITest) {
      m_name = ((ITest) instance).getTestName();
      if (m_name != null) {
        return;
      }
      m_name = m_method.getMethodName();
      if (Utils.getVerbose() > 1) {
        String msg =
            String.format(
                "Warning: [%s] implementation on class [%s] returned null. Defaulting to method name",
                ITest.class.getName(), instance.getClass().getName());
        System.err.println(msg);
      }
      return;
    }
    if (method.getTestClass().getTestName() != null) {
      m_name = method.getTestClass().getTestName();
      return;
    }
    String string = instance.toString();
    // Only display toString() if it's been overridden by the user
    m_name = getMethod().getMethodName();
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
  public String getTestName() {
    if (this.m_method == null) {
      return null;
    }
    Object instance = this.m_method.getInstance();
    if (instance instanceof ITest) {
      return ((ITest) instance).getTestName();
    }
    if (m_method.getTestClass().getTestName() != null) {
      return m_method.getTestClass().getTestName();
    }
    return null;
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

  /** @param method The method to set. */
  public void setMethod(ITestNGMethod method) {
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
    return m_method.getTestClass();
  }

  /** @return Returns the throwable. */
  @Override
  public Throwable getThrowable() {
    return m_throwable;
  }

  /** @param throwable The throwable to set. */
  @Override
  public void setThrowable(Throwable throwable) {
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
  public String getHost() {
    return m_host;
  }

  public void setHost(String host) {
    m_host = host;
  }

  @Override
  public Object[] getParameters() {
    return m_parameters;
  }

  @Override
  public void setParameters(Object[] parameters) {
    m_parameters = new Object[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      // Copy parameter if possible because user may change it later
      if (parameters[i] instanceof Cloneable) {
        try {
          Method clone = parameters[i].getClass().getDeclaredMethod("clone");
          m_parameters[i] = clone.invoke(parameters[i]);
        } catch (NoSuchMethodException
            | InvocationTargetException
            | IllegalAccessException
            | SecurityException e) {
          m_parameters[i] = parameters[i];
        }
      } else {
        m_parameters[i] = parameters[i];
      }
    }
  }

  @Override
  public Object getInstance() {
    return IParameterInfo.embeddedInstance(this.m_method.getInstance());
  }

  @Override
  public Object[] getFactoryParameters() {
    IParameterInfo instance = this.m_method.getFactoryMethodParamsInfo();
    if (instance != null) {
      return instance.getParameters();
    }
    return new Object[0];
  }

  @Override
  public Object getAttribute(String name) {
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
  public Object removeAttribute(String name) {
    return m_attributes.removeAttribute(name);
  }

  @Override
  public ITestContext getTestContext() {
    return m_context;
  }

  public void setContext(ITestContext context) {
    m_context = context;
  }

  @Override
  public int compareTo(@Nonnull ITestResult comparison) {
    return Long.compare(getStartMillis(), comparison.getStartMillis());
  }

  @Override
  public String getInstanceName() {
    return m_instanceName;
  }

  @Override
  public void setTestName(String name) {
    m_name = name;
  }

  public void setParameterIndex(int parameterIndex) {
    this.parameterIndex = parameterIndex;
  }

  public int getParameterIndex() {
    return parameterIndex;
  }

  public boolean wasRetried() {
    return m_wasRetried;
  }

  public void setWasRetried(boolean wasRetried) {
    this.m_wasRetried = wasRetried;
  }

  public List<ITestNGMethod> getSkipCausedBy() {
    if (this.m_status != SKIP || skipAnalysed) {
      return Collections.unmodifiableList(skippedDueTo);
    }
    skipAnalysed = true;
    // check if there were any config failures
    Set<ITestResult> skippedConfigs = m_context.getFailedConfigurations().getAllResults();
    for (ITestResult skippedConfig : skippedConfigs) {
      if (isGlobalFailure(skippedConfig) || isRelated(skippedConfig)) {
        // If there's a failure in @BeforeTest/@BeforeSuite/@BeforeClass
        // then the reason is most often just one method.
        skippedDueTo.add(skippedConfig.getMethod());
      }
      if (belongToSameGroup(skippedConfig)) {
        // If its @BeforeGroups then there's a chance that there could be more than one
        // method. So lets add everything.
        skippedDueTo.add(skippedConfig.getMethod());
      }
    }
    if (!skippedDueTo.isEmpty()) {
      // If we found atleast one skipped due to reason, then its time to return back.
      return Collections.unmodifiableList(skippedDueTo);
    }
    // Looks like we didnt have any configuration failures. So some upstream method perhaps failed.
    if (m_method.getMethodsDependedUpon().length == 0) {
      // Maybe group dependencies exist ?
      if (m_method.getGroupsDependedUpon().length == 0) {
        return Collections.emptyList();
      }
      List<String> upstreamGroups = Arrays.asList(m_method.getGroupsDependedUpon());
      List<ITestResult> allFailures =
          Lists.merge(
              m_context.getFailedTests().getAllResults(),
              m_context.getFailedButWithinSuccessPercentageTests().getAllResults());
      skippedDueTo =
          allFailures.stream()
              .map(ITestResult::getMethod)
              .filter(
                  method -> {
                    List<String> currentMethodGroups = Arrays.asList(method.getGroups());
                    List<String> interection =
                        Lists.intersection(upstreamGroups, currentMethodGroups);
                    return !interection.isEmpty();
                  })
              .collect(Collectors.toList());

      return Collections.unmodifiableList(skippedDueTo);
    }
    List<String> upstreamMethods = Arrays.asList(m_method.getMethodsDependedUpon());

    // So we have dependsOnMethod failures
    List<ITestResult> allfailures =
        Lists.merge(
            m_context.getFailedTests().getAllResults(),
            m_context.getFailedButWithinSuccessPercentageTests().getAllResults());
    skippedDueTo =
        allfailures.stream()
            .map(ITestResult::getMethod)
            .filter(method -> upstreamMethods.contains(method.getQualifiedName()))
            .collect(Collectors.toList());
    return Collections.unmodifiableList(skippedDueTo);
  }

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
    return current.getClass().isAssignableFrom(thatObject.getClass())
        || thatObject.getClass().isAssignableFrom(current.getClass());
  }

  private boolean belongToSameGroup(ITestResult result) {
    ITestNGMethod m = result.getMethod();
    if (!m.isBeforeGroupsConfiguration()) {
      return false;
    }
    String[] mygroups = this.m_method.getGroups();
    if (mygroups.length == 0 || m.getGroups().length == 0) {
      return false;
    }

    List<String> cfgMethodGroups = Arrays.asList(m.getGroups());
    return Arrays.stream(mygroups).anyMatch(cfgMethodGroups::contains);
  }

  public static void copyAttributes(ITestResult source, ITestResult target) {
    source
        .getAttributeNames()
        .forEach(name -> target.setAttribute(name, source.getAttribute(name)));
  }
}
