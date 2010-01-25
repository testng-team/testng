package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.NoOpTestClass;
import org.testng.internal.RunInfo;
import org.testng.internal.TestNGMethod;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
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
public class TestClass extends NoOpTestClass implements ITestClass {
	/* generated */
	private static final long serialVersionUID = -8077917128278361294L;
  transient private IAnnotationFinder m_annotationFinder = null;
  // The Strategy used to locate test methods (TestNG, JUnit, etc...)
  transient private ITestMethodFinder m_testMethodFinder = null;

  transient protected Map<Class, Class> m_testClasses = Maps.newHashMap();
  transient protected Map<Class, Object[]> m_instanceMap = Maps.newHashMap();
  
  private IClass m_iClass = null;
  private RunInfo m_runInfo = null;
  private XmlTest m_xmlTest;
  
  public TestClass(IClass cls, 
                   String testName,
                   ITestMethodFinder testMethodFinder, 
                   IAnnotationFinder annotationFinder,
                   RunInfo runInfo,
                   XmlTest xmlTest) {
    init(cls, testName, testMethodFinder, annotationFinder, runInfo, xmlTest);
  }
  
  public TestClass(IClass cls, TestClass tc) {
    init(cls, 
         tc.getTestName(), 
         tc.getTestMethodFinder(), 
         tc.getAnnotationFinder(),
         tc.getRunInfo(),
         tc.getXmlTest());
  }
  
  public String getTestName() {
    return m_testName;
  }
  
  public XmlTest getXmlTest() {
    return m_xmlTest;
  }
  
  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }
  
  private void init(IClass cls, 
                    String testName,
                    ITestMethodFinder testMethodFinder, 
                    IAnnotationFinder annotationFinder,
                    RunInfo runInfo,
                    XmlTest xmlTest) 
  {
    log(3, "Creating TestClass for " + cls);
    m_iClass = cls;
    m_testClass = cls.getRealClass();
    m_testName = testName;
    m_runInfo = runInfo;
    m_testMethodFinder = testMethodFinder;
    m_annotationFinder = annotationFinder;
    m_xmlTest = xmlTest;
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
      if (instance instanceof ITest) {
        m_testName = ((ITest) instance).getTestName();
      }
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
    ITestNGMethod[] methods = m_testMethodFinder.getTestMethods(m_testClass, m_xmlTest);
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
    List<ITestNGMethod> vResult = Lists.newArrayList();
    for (ITestNGMethod tm : methods) {
      Method m = tm.getMethod();
      if (m.getDeclaringClass().isAssignableFrom(m_testClass)) {
        log(4, "Adding method " + tm + " on TestClass " + m_testClass);
        vResult.add(new TestNGMethod(/* tm.getRealClass(), */ m, m_annotationFinder, m_xmlTest));
      }
      else {
        log(4, "Rejecting method " + tm + " for TestClass " + m_testClass);
      }
    }
    
    ITestNGMethod[] result = vResult.toArray(new ITestNGMethod[vResult.size()]);
    return result;
  }
  
  private RunInfo getRunInfo() {
    return m_runInfo;
  }
    
  public ITestMethodFinder getTestMethodFinder() {
    return m_testMethodFinder;
  }
  
  private void log(int level, String s) {
    Utils.log("TestClass", level, s);
  }
  
  private static void ppp(String s) {
    System.out.println("[TestClass] " + s);
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
  
  @Override
  public String toString() {
    return "[TestClass " + m_testClass + "]";
  }
  
}