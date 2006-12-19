package org.testng.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.IClass;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.TestClass;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.ITestOrConfiguration;
import org.testng.internal.thread.IAtomicInteger;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Superclass to represent both &#64;Test and &#64;Configuration methods.
 */
public abstract class BaseTestMethod implements ITestNGMethod {
  /** The test class on which the test method was found. Note that this is not 
   * necessarily the declaring class. */
  protected ITestClass m_testClass;
  private IClass m_iClass;
  
  protected final transient Class m_methodClass;
  protected final transient Method m_method;
  protected String m_id = "";
  protected long m_date = System.currentTimeMillis();
  protected final transient IAnnotationFinder m_annotationFinder;
  protected String[] m_groups = {};
  protected String[] m_groupsDependedUpon = {};
  protected String[] m_methodsDependedUpon = {};
  protected String[] m_beforeGroups = {};
  protected String[] m_afterGroups = {};
  private boolean m_isAlwaysRun;
  
  // Methods are not serialized, but we can serialize their hashCode  
  private final String m_signature;
  private final String m_methodName;
  // If a depended group is not found
  private String m_missingGroup;
  private String m_description = null;
  protected IAtomicInteger m_currentInvocationCount = ThreadUtil.createAtomicInteger(0);
  private int m_parameterInvocationCount = 1;
  
  /**
   * Constructs a <code>BaseTestMethod</code> TODO cquezel JavaDoc.
   *
   * @param method
   * @param annotationFinder
   */
  public BaseTestMethod(Method method, IAnnotationFinder annotationFinder) {
    m_methodClass = method.getDeclaringClass();
    m_method = method;
    m_methodName = m_method.getName();
    m_annotationFinder = annotationFinder;
    m_signature = initSignature();
  }

  private void initTestClass() {
    
  }
  
  /**
   * {@inheritDoc}
   */
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
  public Class getRealClass() {
    return m_methodClass;
  }

  /**
   * {@inheritDoc}
   */
  public ITestClass getTestClass() {
    return m_testClass;
  }

  /**
   * {@inheritDoc}
   */
  public void setTestClass(ITestClass tc) {
//    assert null != tc;
    Assert.assertNotNull(tc, "ITestClass cannot be null");
    if(!m_method.getDeclaringClass().isAssignableFrom(tc.getRealClass())) {
      TestClass casted= (TestClass) tc;
      m_iClass= new ClassImpl(m_method.getDeclaringClass(), null, new HashMap<Class, IClass>(),
          new XmlTest(new XmlSuite()), m_annotationFinder);
      m_testClass= new TestClass(m_iClass, casted.getTestName(), casted.getTestMethodFinder(),  m_annotationFinder, 
          casted.getRunInfo() , casted.getTestRunner());

    }
    else {
//    Assert.assertTrue(m_method.getDeclaringClass().isAssignableFrom(tc.getRealClass()),
//        "Method declaration class is not assignable from the " + tc.getRealClass());
//    if (! tc.getRealClass().equals(m_method.getDeclaringClass())) {
//      assert m_method.getDeclaringClass().isAssignableFrom(tc.getRealClass()) :
//        "\nMISMATCH : " + tc.getRealClass() + " " + m_method.getDeclaringClass();
//    }
      m_testClass = tc;
    }
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @param o
   * @return
   */
  public int compareTo(Object o) {
    int result = -2;
    Class thisClass = getRealClass();
    Class otherClass = ((ITestNGMethod) o).getRealClass();
    if (thisClass.isAssignableFrom(otherClass)) 
      result = -1;
    else if (otherClass.isAssignableFrom(thisClass)) 
      result = 1;
    else if (equals(o)) 
      result = 0;
    
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public Method getMethod() {
    return m_method;
  }

  /**
   * {@inheritDoc}
   */
  public String getMethodName() {
    return m_methodName;
  }

  /**
   * {@inheritDoc}
   */
  public Object[] getInstances() {
    return m_testClass.getInstances(false);
  }

  /**
   * {@inheritDoc}
   */
  public long[] getInstanceHashCodes() {
    return m_testClass.getInstanceHashCodes();
  }

  /**
   * {@inheritDoc}
   * @return the addition of groups defined on the class and on this method.
   */
  public String[] getGroups() {
    return m_groups;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getGroupsDependedUpon() {
    return m_groupsDependedUpon;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getMethodsDependedUpon() {
    return m_methodsDependedUpon;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isTest() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBeforeSuiteConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAfterSuiteConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBeforeTestConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAfterTestConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBeforeGroupsConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAfterGroupsConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBeforeClassConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAfterClassConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBeforeMethodConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAfterMethodConfiguration() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public long getTimeOut() {
    return 0L;
  }
  
  /**
   * {@inheritDoc}  
   * @return the number of times this method needs to be invoked.
   */
  public int getInvocationCount() {
    return 1;
  }
  
  /**
   * No-op.
   */
  public void setInvocationCount(int counter) {
  }

  /**
   * {@inheritDoc} Default value for successPercentage.
   */
  public int getSuccessPercentage() {
    return 100;
  }

  /**
   * {@inheritDoc}
   */
  public String getId() {
    return m_id;
  }

  /**
   * {@inheritDoc}
   */
  public void setId(String id) {
    m_id = id;
  }
  

  /**
   * {@inheritDoc} 
   * @return Returns the date.
   */
  public long getDate() {
    return m_date;
  }

  /**
   * {@inheritDoc} 
   * @param date The date to set.
   */
  public void setDate(long date) {
    m_date = date;
  }

  /**
   * {@inheritDoc}
   */
  public boolean canRunFromClass(IClass testClass) {
    return m_methodClass.isAssignableFrom(testClass.getRealClass());
  }

  /**
   * {@inheritDoc} Compares two BaseTestMethod using the test class then the associated 
   * Java Method. 
   */
  @Override
  public boolean equals(Object obj) {
    // TODO CQ document why this try block exists.
    try {
      BaseTestMethod other = (BaseTestMethod) obj;
      
      boolean isEqual = m_testClass == null ? other.m_testClass == null
          : m_testClass.getRealClass().equals(other.m_testClass.getRealClass());
      
      return isEqual && m_method.equals(other.m_method);
    }
    catch(Exception ex) {
      return false;
    }
  }
  
  /**
   * {@inheritDoc} This implementation returns the associated Java Method's hash code.
   * @Return the associated Java Method's hash code. 
   */
  @Override
  public int hashCode() {
    return m_method.hashCode();
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @param annotationClass
   */
  protected void initGroups(Class annotationClass) {
    //
    // Init groups
    //
    {
      ITestOrConfiguration annotation = 
        (ITestOrConfiguration) getAnnotationFinder().findAnnotation(getMethod(),
          annotationClass);
      ITestOrConfiguration classAnnotation = 
        (ITestOrConfiguration) getAnnotationFinder().findAnnotation(getMethod().getDeclaringClass(),
          annotationClass);
      
      setGroups(getStringArray(null != annotation ? annotation.getGroups() : null, 
          null != classAnnotation ? classAnnotation.getGroups() : null));    
    }
    
    //
    // Init groups depended upon
    //
    {
      ITestOrConfiguration annotation = 
        (ITestOrConfiguration) getAnnotationFinder().findAnnotation(getMethod(),
          annotationClass);
      ITestOrConfiguration classAnnotation = 
        (ITestOrConfiguration) getAnnotationFinder().findAnnotation(getMethod().getDeclaringClass(),
          annotationClass);
  
      setGroupsDependedUpon(
          getStringArray(null != annotation ? annotation.getDependsOnGroups() : null, 
          null != classAnnotation ? classAnnotation.getDependsOnGroups() : null));
      
      String[] methodsDependedUpon =
        getStringArray(null != annotation ? annotation.getDependsOnMethods() : null, 
        null != classAnnotation ? classAnnotation.getDependsOnMethods() : null);
      // Qualify these methods if they don't have a package
      for (int i = 0; i < methodsDependedUpon.length; i++) {
        String m = methodsDependedUpon[i];
        if (m.indexOf(".") < 0) {
          methodsDependedUpon[i] = 
            MethodHelper.calculateMethodCanonicalName(m_methodClass, methodsDependedUpon[i]);
        }
      }
      setMethodsDependedUpon(methodsDependedUpon);
    }
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @return
   */
  protected IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }
  
  /**
   * TODO cquezel JavaDoc.
   *
   * @return
   */
  protected IClass getIClass() {
    return m_testClass;
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @return
   */
  protected String getSignature() {
    return m_signature;
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @return
   */
  private String initSignature() {
    Method m = getMethod();
    String cls = m.getDeclaringClass().getName();
    StringBuffer result = new StringBuffer(cls + "." + m.getName() + "(");
    int i = 0;
    for (Class p : m.getParameterTypes()) {
      if (i++ > 0) result.append(", ");
      result.append(p.getName());
    }
    result.append(")");

    return result.toString();
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getSignature();
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @param methodArray
   * @param classArray
   * @return
   */
  protected String[] getStringArray(String[] methodArray, String[] classArray) {
    List<String> vResult = new ArrayList<String>();
    if (null != methodArray) {
      for (String m : methodArray) {
        vResult.add(m);
      }
    }
    if (null != classArray) {
      for (String m : classArray) {
        vResult.add(m);
      }
    }
    
    return vResult.toArray(new String[vResult.size()]);
  }

  protected void setGroups(String[] groups) {
    m_groups = groups;
  }
  
  protected void setGroupsDependedUpon(String[] groups) {
    m_groupsDependedUpon = groups;
  }
  
  protected void setMethodsDependedUpon(String[] methods) {
    m_methodsDependedUpon = methods;
  }
  
  /**
   * {@inheritDoc}
   */
  public void addMethodDependedUpon(String method) {
    String[] newMethods = new String[m_methodsDependedUpon.length + 1];
    newMethods[0] = method;
    for (int i =1; i < newMethods.length; i++) {
      newMethods[i] = m_methodsDependedUpon[i - 1];
    }
    m_methodsDependedUpon = newMethods;
  }

  private static void ppp(String s) {
    System.out.println("[BaseTestMethod] " + s);
  }

  /** Compares two ITestNGMethod by date. */
  public static final Comparator DATE_COMPARATOR = new Comparator() {
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
  public String getMissingGroup() {
    return m_missingGroup;
  }

  /**
   * {@inheritDoc}
   */
  public void setMissingGroup(String group) {
    m_missingGroup = group;
  }

  /**
   * {@inheritDoc}
   */
  public int getThreadPoolSize() {
    return 0;
  }

  /**
   * No-op.
   * @param threadPoolSize
   */
  public void setThreadPoolSize(int threadPoolSize) {
  }

  /**
   * TODO cquezel JavaDoc.
   *
   * @param description
   */
  public void setDescription(String description) {
    m_description = description;
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription() {
    return m_description;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getBeforeGroups() {
    return m_beforeGroups;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getAfterGroups() {
    return m_afterGroups;
  }
  
  public void incrementCurrentInvocationCount() {
    m_currentInvocationCount.incrementAndGet();
  }
  
  public int getCurrentInvocationCount() {
    return m_currentInvocationCount.get();
  }

  public void setParameterInvocationCount(int n) {
    m_parameterInvocationCount = n;
  }
  
  public int getParameterInvocationCount() {
    return m_parameterInvocationCount;
  }  
  
  public abstract ITestNGMethod clone();
}
