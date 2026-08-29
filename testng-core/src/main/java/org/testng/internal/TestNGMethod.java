package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.testng.IDataProviderMethod;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.annotations.CustomAttribute;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

/** This class represents a test method. */
public class TestNGMethod extends BaseTestMethod {

  private int m_threadPoolSize = 0;
  private int m_invocationCount = 1;
  private int m_successPercentage = 100;
  private boolean isDataDriven = false;
  private CustomAttribute[] m_attributes = {};
  private @Nullable IDataProviderMethod dataProviderMethod = null;

  /** Constructs a <code>TestNGMethod</code> */
  public TestNGMethod(
      ITestObjectFactory objectFactory,
      Method method,
      IAnnotationFinder finder,
      XmlTest xmlTest,
      IObject.@Nullable IdentifiableObject instance) {
    this(objectFactory, method, finder, instance);
    init(xmlTest);
  }

  /**
   * Builds the method without initialising it from an {@link XmlTest}; {@link #clone()} copies the
   * state across itself.
   */
  private TestNGMethod(
      ITestObjectFactory objectFactory,
      Method method,
      IAnnotationFinder finder,
      IObject.@Nullable IdentifiableObject instance) {
    super(objectFactory, method.getName(), new ConstructorOrMethod(method), finder, instance);
  }

  /** {@inheritDoc} */
  @Override
  public int getInvocationCount() {
    return m_invocationCount;
  }

  /** {@inheritDoc} */
  @Override
  public int getSuccessPercentage() {
    return m_successPercentage;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isTest() {
    return true;
  }

  private void init(XmlTest xmlTest) {
    setXmlTest(xmlTest);
    String className = m_method.getDeclaringClass().getName();
    if (isInstanceInstantiated()) {
      Object obj = getInstance();
      if (obj != null) {
        className = obj.getClass().getName();
      }
    } else {
      // Lazy @Factory instance not created yet: a constructor factory produces exactly its
      // declaring/real class, so use that instead of instantiating the instance to read its class.
      className = getRealClass().getName();
    }
    setInvocationNumbers(xmlTest.getInvocationNumbers(className + "." + m_method.getName()));

    ITestAnnotation testAnnotation =
        AnnotationHelper.findTest(getAnnotationFinder(), m_method.requireMethod());

    if (testAnnotation == null) {
      // Try on the class
      testAnnotation =
          AnnotationHelper.findTest(getAnnotationFinder(), m_method.getDeclaringClass());
    }

    if (null != testAnnotation) {
      setTimeOut(testAnnotation.getTimeOut());
      m_successPercentage = testAnnotation.getSuccessPercentage();
      isDataDriven = doesTestAnnotationHaveADataProvider(testAnnotation);

      setInvocationCount(testAnnotation.getInvocationCount());
      setThreadPoolSize(testAnnotation.getThreadPoolSize());
      setAlwaysRun(testAnnotation.getAlwaysRun());
      setDescription(findDescription(testAnnotation, xmlTest));
      setEnabled(testAnnotation.getEnabled());
      setRetryAnalyzerClass(testAnnotation.getRetryAnalyzerClass());
      setSkipFailedInvocations(testAnnotation.skipFailedInvocations());
      setInvocationTimeOut(testAnnotation.invocationTimeOut());
      setIgnoreMissingDependencies(testAnnotation.ignoreMissingDependencies());
      setPriority(testAnnotation.getPriority());
      m_attributes = testAnnotation.getAttributes();
    }

    // Groups
    initGroups(ITestAnnotation.class);
  }

  private static boolean doesTestAnnotationHaveADataProvider(ITestAnnotation testAnnotation) {
    return !testAnnotation.getDataProvider().trim().isEmpty()
        || testAnnotation.getDataProviderClass() != null;
  }

  private String findDescription(ITestAnnotation testAnnotation, XmlTest xmlTest) {
    String result = testAnnotation.getDescription();
    if (result != null) {
      return result;
    }
    List<XmlClass> classes = xmlTest.getXmlClasses();
    return classes.stream()
        .filter(this::classNameMatcher)
        .flatMap(xmlClass -> xmlClass.getIncludedMethods().stream())
        .filter(this::methodNameMatcher)
        .map(XmlInclude::getDescription)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse("");
  }

  private boolean classNameMatcher(XmlClass xmlClass) {
    return xmlClass.getName().equals(m_method.getDeclaringClass().getName());
  }

  private boolean methodNameMatcher(XmlInclude xmlInclude) {
    return xmlInclude.getName().equals(m_method.getName());
  }

  /** {@inheritDoc} */
  @Override
  public int getThreadPoolSize() {
    return m_threadPoolSize;
  }

  /** Sets the number of threads on which this method should be invoked. */
  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    m_threadPoolSize = threadPoolSize;
  }

  /** Sets the number of invocations for this method. */
  @Override
  public void setInvocationCount(int counter) {
    m_invocationCount = counter;
  }

  /**
   * Binds a fully initialized prototype to another instance. When the instance's effective class
   * for group lookup matches the prototype's, the expensive {@link #init(XmlTest)} work is reused.
   */
  public TestNGMethod bind(IObject.IdentifiableObject instance) {
    TestNGMethod bound =
        new TestNGMethod(
            m_objectFactory,
            getConstructorOrMethod().requireMethod(),
            getAnnotationFinder(),
            instance);
    copyInitializedState(bound);
    XmlTest xmlTest = getXmlTest();
    if (xmlTest != null
        && !effectiveClassForGroups().equals(bound.effectiveClassForGroups())) {
      bound.init(xmlTest);
    }
    return bound;
  }

  private void copyInitializedState(TestNGMethod target) {
    target.setXmlTest(getXmlTest());
    target.setXmlOccurrence(getXmlClass(), getXmlInclude(), getXmlOccurrenceIndex());
    ITestClass tc = getTestClass();
    if (tc != null) {
      // Wrapping a test class this method has not been bound to yet would have thrown here.
      // ConfigurationMethod.clone() already propagates the absence rather than wrapping it.
      NoOpTestClass testClass = new NoOpTestClass(tc);
      testClass.setBeforeTestMethods(clone(tc.getBeforeTestMethods()));
      testClass.setAfterTestMethod(clone(tc.getAfterTestMethods()));
      target.m_testClass = testClass;
    }
    target.setDate(getDate());
    target.setGroups(getGroups());
    target.setGroupsDependedUpon(getGroupsDependedUpon(), Collections.emptyList());
    target.setMethodsDependedUpon(getMethodsDependedUpon());
    target.setAlwaysRun(isAlwaysRun());
    target.m_beforeGroups = getBeforeGroups();
    target.m_afterGroups = getAfterGroups();
    target.setMissingGroup(getMissingGroup());
    target.setThreadPoolSize(getThreadPoolSize());
    target.setDescription(getDescription());
    target.setEnabled(getEnabled());
    target.setParameterInvocationCount(getParameterInvocationCount());
    target.setInvocationCount(getInvocationCount());
    target.m_successPercentage = getSuccessPercentage();
    target.setTimeOut(getTimeOut());
    target.setRetryAnalyzerClass(getRetryAnalyzerClass());
    target.setSkipFailedInvocations(skipFailedInvocations());
    target.setInvocationNumbers(getInvocationNumbers());
    target.setPriority(getPriority());
    target.m_attributes = m_attributes;
    target.isDataDriven = isDataDriven;
    target.setIgnoreMissingDependencies(ignoreMissingDependencies());
    target.setInvocationTimeOut(getInvocationTimeOut());
  }

  /**
   * Clones the current <code>TestNGMethod</code> and its @BeforeMethod and @AfterMethod methods.
   *
   * @see org.testng.internal.BaseTestMethod#clone()
   */
  @Override
  public BaseTestMethod clone() {
    TestNGMethod clone =
        new TestNGMethod(
            m_objectFactory,
            getConstructorOrMethod().requireMethod(),
            getAnnotationFinder(),
            cloneInstance());
    copyInitializedState(clone);
    clone.m_currentInvocationCount = m_currentInvocationCount;
    return clone;
  }

  private static ITestNGMethod[] clone(ITestNGMethod[] sources) {
    return Arrays.stream(sources).map(ITestNGMethod::clone).toArray(ITestNGMethod[]::new);
  }

  @Override
  public boolean isDataDriven() {
    return isDataDriven;
  }

  @Override
  public CustomAttribute[] getAttributes() {
    return m_attributes;
  }

  @Override
  public @Nullable IDataProviderMethod getDataProviderMethod() {
    return dataProviderMethod;
  }

  public void setDataProviderMethod(@Nullable IDataProviderMethod dataProviderMethod) {
    this.dataProviderMethod = dataProviderMethod;
  }
}
