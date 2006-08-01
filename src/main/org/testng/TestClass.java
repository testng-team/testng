package org.testng;

import org.testng.internal.ConfigurationMethod;
import org.testng.internal.RunInfo;
import org.testng.internal.TestNGMethod;
import org.testng.internal.annotations.IAnnotationFinder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a test class:
 * - The test methods
 * - The configuration methods (test and method)
 * - The class file
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestClass implements ITestClass {
	/* generated */
	private static final long serialVersionUID = -8077917128278361294L;
private IClass m_iClass = null;
  private Class m_testClass = null;
  transient private IAnnotationFinder m_annotationFinder = null;
  // Information about this test run
  private String m_testName = null;
  
  // The Strategy used to locate test methods (TestNG, JUnit, etc...)
  transient private ITestMethodFinder m_testMethodFinder = null;
  
  // Test methods
  private ITestNGMethod[] m_beforeClassMethods = null;
  private ITestNGMethod[] m_beforeTestMethods = null;
  private ITestNGMethod[] m_testMethods = null;
  private ITestNGMethod[] m_afterTestMethods = null;
  private ITestNGMethod[] m_afterClassMethods = null;
  private ITestNGMethod[] m_beforeSuiteMethods = null;
  private ITestNGMethod[] m_afterSuiteMethods = null;
  private ITestNGMethod[] m_beforeTestConfMethods = null;
  private ITestNGMethod[] m_afterTestConfMethods = null;
  private ITestNGMethod[] m_beforeGroupsMethods = null;
  private ITestNGMethod[] m_afterGroupsMethods = null;
  
  transient private Map<Class, Class> m_testClasses = new HashMap<Class, Class>();
  transient private Map<Class, Object[]> m_instanceMap = new HashMap<Class, Object[]>();
  
  private RunInfo m_runInfo = null;

  private TestRunner m_testRunner = null;
  
  public TestClass(IClass cls, 
                   String testName,
                   ITestMethodFinder testMethodFinder, 
                   IAnnotationFinder annotationFinder,
                   RunInfo runInfo, 
                   TestRunner testRunner) {
    init(cls, testName, testMethodFinder, annotationFinder, runInfo, testRunner);
  }
  
  public TestClass(IClass cls, TestClass tc) {
    init(cls, 
         tc.getTestName(), 
         tc.getTestMethodFinder(), 
         tc.getAnnotationFinder(),
         tc.getRunInfo(), 
         tc.getTestRunner());
  }
  
  public String getTestName() {
    return m_testName;
  }
  
  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }
  
  private void init(IClass cls, 
                    String testName,
                    ITestMethodFinder testMethodFinder, 
                    IAnnotationFinder annotationFinder,
                    RunInfo runInfo, 
                    TestRunner testRunner) {
    log(3, "Creating TestClass for " + cls);
    m_iClass = cls;
    m_testClass = cls.getRealClass();
    m_testName = testName;
    m_runInfo = runInfo;
    m_testRunner = testRunner;
    m_testMethodFinder = testMethodFinder;
    m_annotationFinder = annotationFinder;
//    m_testMethodFinder.setTestClass(this);
    initMethods();
    initTestClassesAndInstances();
  }
  
  private void initTestClassesAndInstances() {
    //
    // TestClasses and instances
    //
   Object[] instances = getInstances(false);
    for (Object instance : instances) {
      Class cls = instance.getClass();
      if (null == m_testClasses.get(cls)) {
        m_testClasses.put(cls, cls);
        m_instanceMap.put(cls, instances);
      }
    }
    
  }
    
  public Object[] getInstances(boolean create) {
    return m_iClass.getInstances(create);
  }
  
  public long[] getInstanceHashCodes() {
    return m_iClass.getInstanceHashCodes();
  }
  
  public int getInstanceCount() {
    return m_iClass.getInstanceCount();
  }
  
  public void addInstance(Object instance) {
    m_iClass.addInstance(instance);
  }

  private void initMethods() {
    ITestNGMethod[] methods = m_testMethodFinder.getTestMethods(m_testClass);
    m_testMethods = createTestMethods(methods);

    m_beforeSuiteMethods = ConfigurationMethod
        .createSuiteConfigurationMethods(m_testMethodFinder.getBeforeSuiteMethods(m_testClass),
                                         m_annotationFinder,
                                         true);
    m_afterSuiteMethods = ConfigurationMethod
        .createSuiteConfigurationMethods(m_testMethodFinder.getAfterSuiteMethods(m_testClass), 
                                         m_annotationFinder,
                                         false); 


    m_beforeTestConfMethods = ConfigurationMethod 
        .createTestConfigurationMethods(m_testMethodFinder.getBeforeTestConfigurationMethods(m_testClass), 
                                        m_annotationFinder,
                                        true);
    m_afterTestConfMethods = ConfigurationMethod
        .createTestConfigurationMethods(m_testMethodFinder.getAfterTestConfigurationMethods(m_testClass), 
                                        m_annotationFinder,
                                        false);

    m_beforeClassMethods = ConfigurationMethod
        .createClassConfigurationMethods(m_testMethodFinder.getBeforeClassMethods(m_testClass), 
                                         m_annotationFinder,
                                         true);
    m_afterClassMethods = ConfigurationMethod
        .createClassConfigurationMethods(m_testMethodFinder.getAfterClassMethods(m_testClass), 
                                         m_annotationFinder,
                                         false);
    
    m_beforeGroupsMethods = ConfigurationMethod
    .createBeforeConfigurationMethods(m_testMethodFinder.getBeforeGroupsConfigurationMethods(m_testClass), 
                                          m_annotationFinder,
                                          true);
    m_afterGroupsMethods = ConfigurationMethod
    .createAfterConfigurationMethods(m_testMethodFinder.getAfterGroupsConfigurationMethods(m_testClass),
                                          m_annotationFinder,
                                          false); 
    


    m_beforeTestMethods = ConfigurationMethod
        .createTestMethodConfigurationMethods(m_testMethodFinder.getBeforeTestMethods(m_testClass), 
                                              m_annotationFinder,
                                              true);
    m_afterTestMethods = ConfigurationMethod
        .createTestMethodConfigurationMethods(m_testMethodFinder.getAfterTestMethods(m_testClass),
                                              m_annotationFinder,
                                              false);
    
  }
  
  /**
   * Create the test methods that belong to this class (rejects
   * all those that belong to a different class).
   */
  private ITestNGMethod[] createTestMethods(ITestNGMethod[] methods) {
    List<ITestNGMethod> vResult = new ArrayList<ITestNGMethod>();
    for (ITestNGMethod tm : methods) {
      Method m = tm.getMethod();
      if (m.getDeclaringClass().isAssignableFrom(m_testClass)) {
        log(3, "Adding method " + tm + " on TestClass " + m_testClass);
        vResult.add(new TestNGMethod(/* tm.getRealClass(), */ m, m_annotationFinder));
      }
      else {
        log(3, "Rejecting method " + tm + " for TestClass " + m_testClass);
      }
    }
    
    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);
    return result;
  }
  
  private TestRunner getTestRunner() {
    return m_testRunner;
  }
  
  private RunInfo getRunInfo() {
    return m_runInfo;
  }
    
  public ITestMethodFinder getTestMethodFinder() {
    return m_testMethodFinder;
  }
  
  private void log(int level, String s) {
    if (TestRunner.getVerbose() >= level) {
      ppp(s);
    }
  }
  
  private static void ppp(String s) {
    System.out.println("[TestClass] " + s);
  }

  public String getName() {
    return m_testClass.getName();
  }
  
  public Class getRealClass() {
    return m_testClass;
  }
  
  public Class[] getTestClasses() {
    return m_testClasses.keySet().toArray(new Class[m_testClasses.size()]);
  }
  
  public void dump() {
    ppp("\n======\nTESTCLASS: " + m_testClass.getName());
    for (ITestNGMethod m : m_beforeClassMethods) {
      ppp("BeforeClass : " + m);
    }
    for (ITestNGMethod m : m_beforeTestMethods) {
      ppp("BeforeMethod:\t" + m);
    }
    for (ITestNGMethod m : m_testMethods) {
      ppp("Test        :\t\t" + m);
    }
    for (ITestNGMethod m : m_afterTestMethods) {
      ppp("AfterMethod :\t" + m);
    }
    for (ITestNGMethod m : m_afterClassMethods) {
      ppp("AfterClass  : " + m);
    }
    ppp("\n======\n");
  }
  
  /**
   * @return Returns the afterClassMethods.
   */
  public ITestNGMethod[] getAfterClassMethods() {
    return m_afterClassMethods;
  }
  /**
   * @return Returns the afterTestMethods.
   */
  public ITestNGMethod[] getAfterTestMethods() {
    return m_afterTestMethods;
  }
  /**
   * @return Returns the beforeClassMethods.
   */
  public ITestNGMethod[] getBeforeClassMethods() {
    return m_beforeClassMethods;
  }
  /**
   * @return Returns the beforeTestMethods.
   */
  public ITestNGMethod[] getBeforeTestMethods() {
    return m_beforeTestMethods;
  }
  /**
   * @return Returns the testMethods.
   */
  public ITestNGMethod[] getTestMethods() {
    return m_testMethods;
  }
  
  public ITestNGMethod[] getBeforeSuiteMethods() {
    return m_beforeSuiteMethods;
  }
  
  public ITestNGMethod[] getAfterSuiteMethods() {
    return m_afterSuiteMethods;
  }

  public ITestNGMethod[] getBeforeTestConfigurationMethods() {
    return m_beforeTestConfMethods;
  }

  public ITestNGMethod[] getAfterTestConfigurationMethods() {
    return m_afterTestConfMethods;
  }
  
  /**
   * @return all @Configuration methods that should be invoked before certain groups
   */
  public ITestNGMethod[] getBeforeGroupsMethods() {
    return m_beforeGroupsMethods;
  }

  /**
   * @return all @Configuration methods that should be invoked after certain groups
   */
  public ITestNGMethod[] getAfterGroupsMethods() {
    return m_afterGroupsMethods;    
  }

  
  @Override
  public String toString() {
    return "[TestClass " + m_testClass + "]";
  }
}
