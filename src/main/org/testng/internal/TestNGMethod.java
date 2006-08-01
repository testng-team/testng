package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Comparator;

import org.testng.ITestNGMethod;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.ITest;


/**
 * This class represents a test method.
 *
 * @author Cedric Beust, May 3, 2004
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public class TestNGMethod extends BaseTestMethod {
  private int m_invocationCount = 1;
  private int m_successPercentage = 100;
  private long m_timeOut = 0;

  /**
   * Constructs a <code>TestNGMethod</code> TODO cquezel JavaDoc.
   *
   * @param method
   * @param finder
   */
  public TestNGMethod(Method method, IAnnotationFinder finder) {
    super(method, finder);
    
    init();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTimeOut() {
    return m_timeOut;
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

  /**
   * TODO cquezel JavaDoc.
   */
  private void init() {
    {
      ITest testAnnotation = AnnotationHelper.findTest(getAnnotationFinder(), m_method);

      if (null != testAnnotation) {
        m_timeOut = testAnnotation.getTimeOut();
      }

      if (null != testAnnotation) {
        m_invocationCount = testAnnotation.getInvocationCount();
        m_successPercentage = testAnnotation.getSuccessPercentage();
        
        // Integer.valueOf would be much better here but it is jdk5+ specific
        setThreadPoolSize(new Integer(testAnnotation.getThreadPoolSize()));
      }

      // Groups
      {
        initGroups(ITest.class);
      }

      // Other annotations
      if (null != testAnnotation) {
        setAlwaysRun(testAnnotation.getAlwaysRun());
        setDescription(testAnnotation.getDescription());
      }
    }
  }

  /** Sorts ITestNGMethod by Class name. */
  public static final Comparator<ITestNGMethod> SORT_BY_CLASS =
    new Comparator<ITestNGMethod>() {
    
    public int compare(ITestNGMethod o1, ITestNGMethod o2) {
      String c1 = o1.getTestClass().getName();
      String c2 = o2.getTestClass().getName();
      return c1.compareTo(c2);
    }
  };
}
