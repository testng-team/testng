package org.testng.internal;

import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlTest;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * This class represents a test method.
 *
 * @author Cedric Beust, May 3, 2004
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public class TestNGMethod extends BaseTestMethod implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = -1742868891986775307L;
  private int m_threadPoolSize = 0;
  private int m_invocationCount = 1;
  private int m_totalInvocationCount = m_invocationCount;
  private int m_successPercentage = 100;

  /**
   * Constructs a <code>TestNGMethod</code>
   *
   * @param method
   * @param finder
   */
  public TestNGMethod(Method method, IAnnotationFinder finder, XmlTest xmlTest, Object instance) {
    this(method, finder, true, xmlTest, instance);
  }

  private TestNGMethod(Method method, IAnnotationFinder finder, boolean initialize,
      XmlTest xmlTest, Object instance) {
    super(method.getName(), method, finder, instance);

    if(initialize) {
      init(xmlTest);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getInvocationCount() {
    return m_invocationCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getTotalInvocationCount() {
    return m_totalInvocationCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSuccessPercentage() {
    return m_successPercentage;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTest() {
    return true;
  }

  private void ppp(String s) {
    System.out.println("[TestNGMethod] " + s);
  }

  private void init(XmlTest xmlTest) {
    setXmlTest(xmlTest);
    setInvocationNumbers(xmlTest.getInvocationNumbers(
        m_method.getDeclaringClass().getName() + "." + m_method.getName()));
    {
      ITestAnnotation testAnnotation =
          AnnotationHelper.findTest(getAnnotationFinder(), m_method.getMethod());

      if (testAnnotation == null) {
        // Try on the class
        testAnnotation = AnnotationHelper.findTest(getAnnotationFinder(), m_method.getDeclaringClass());
      }

      if (null != testAnnotation) {
        setTimeOut(testAnnotation.getTimeOut());
        m_successPercentage = testAnnotation.getSuccessPercentage();

        setInvocationCount(testAnnotation.getInvocationCount());
        m_totalInvocationCount = testAnnotation.getInvocationCount();
        setThreadPoolSize(testAnnotation.getThreadPoolSize());
        setAlwaysRun(testAnnotation.getAlwaysRun());
        setDescription(findDescription(testAnnotation, xmlTest));
        setEnabled(testAnnotation.getEnabled());
        setRetryAnalyzer(testAnnotation.getRetryAnalyzer());
        setSkipFailedInvocations(testAnnotation.skipFailedInvocations());
        setInvocationTimeOut(testAnnotation.invocationTimeOut());
        setIgnoreMissingDependencies(testAnnotation.ignoreMissingDependencies());
        setPriority(testAnnotation.getPriority());
      }

      // Groups
      {
        initGroups(ITestAnnotation.class);
      }
    }
  }

  private String findDescription(ITestAnnotation testAnnotation, XmlTest xmlTest) {
    String result = testAnnotation.getDescription();
    if (result == null) {
      List<XmlClass> classes = xmlTest.getXmlClasses();
      for (XmlClass c : classes) {
        if (c.getName().equals(m_method.getMethod().getDeclaringClass().getName())) {
          for (XmlInclude include : c.getIncludedMethods()) {
            if (include.getName().equals(m_method.getName())) {
              result = include.getDescription();
              if (result != null) {
                break;
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getThreadPoolSize() {
    return m_threadPoolSize;
  }

  /**
   * Sets the number of threads on which this method should be invoked.
   */
  @Override
  public void setThreadPoolSize(int threadPoolSize) {
    m_threadPoolSize = threadPoolSize;
  }

  /**
   * Sets the number of invocations for this method.
   */
  @Override
  public void setInvocationCount(int counter) {
    m_invocationCount= counter;
  }

  /**
   * Clones the current <code>TestNGMethod</code> and its @BeforeMethod and @AfterMethod methods.
   * @see org.testng.internal.BaseTestMethod#clone()
   */
  @Override
  public BaseTestMethod clone() {
    TestNGMethod clone= new TestNGMethod(getMethod(), getAnnotationFinder(), false, getXmlTest(),
        getInstance());
    ITestClass tc= getTestClass();
    NoOpTestClass testClass= new NoOpTestClass(tc);
    testClass.setBeforeTestMethods(clone(tc.getBeforeTestMethods()));
    testClass.setAfterTestMethod(clone(tc.getAfterTestMethods()));
    clone.m_testClass= testClass;
    clone.setDate(getDate());
    clone.setGroups(getGroups());
    clone.setGroupsDependedUpon(getGroupsDependedUpon(), Collections.<String>emptyList());
    clone.setMethodsDependedUpon(getMethodsDependedUpon());
    clone.setAlwaysRun(isAlwaysRun());
    clone.m_beforeGroups= getBeforeGroups();
    clone.m_afterGroups= getAfterGroups();
    clone.m_currentInvocationCount= m_currentInvocationCount;
    clone.setMissingGroup(getMissingGroup());
    clone.setThreadPoolSize(getThreadPoolSize());
    clone.setDescription(getDescription());
    clone.setEnabled(getEnabled());
    clone.setParameterInvocationCount(getParameterInvocationCount());
    clone.setInvocationCount(getInvocationCount());
    clone.m_totalInvocationCount = getTotalInvocationCount();
    clone.m_successPercentage = getSuccessPercentage();
    clone.setTimeOut(getTimeOut());
    clone.setRetryAnalyzer(getRetryAnalyzer());
    clone.setSkipFailedInvocations(skipFailedInvocations());
    clone.setInvocationNumbers(getInvocationNumbers());
    clone.setPriority(getPriority());

    return clone;
  }

  private ITestNGMethod[] clone(ITestNGMethod[] sources) {
    ITestNGMethod[] clones= new ITestNGMethod[sources.length];
    for(int i= 0; i < sources.length; i++) {
      clones[i]= sources[i].clone();
    }

    return clones;
  }

  /** Sorts ITestNGMethod by Class name. */
  public static final Comparator<ITestNGMethod> SORT_BY_CLASS =
    new Comparator<ITestNGMethod>() {

    @Override
    public int compare(ITestNGMethod o1, ITestNGMethod o2) {
      String c1 = o1.getTestClass().getName();
      String c2 = o2.getTestClass().getName();
      return c1.compareTo(c2);
    }
  };
}
