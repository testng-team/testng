package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.NoOpTestClass;
import org.testng.internal.RunInfo;
import org.testng.internal.TestNGMethod;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.lang.reflect.Method;
import java.util.List;

/**
 * This class represents a test class:
 * - The test methods
 * - The configuration methods (test and method)
 * - The class file
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
class TestClass extends NoOpTestClass implements ITestClass {
	/* generated */
	private static final long serialVersionUID = -8077917128278361294L;
  transient private IAnnotationFinder m_annotationFinder = null;
  // The Strategy used to locate test methods (TestNG, JUnit, etc...)
  transient private ITestMethodFinder m_testMethodFinder = null;

  private IClass m_iClass = null;
  private RunInfo m_runInfo = null;
  private String m_testName;
  private XmlTest m_xmlTest;
  private XmlClass m_xmlClass;

  protected TestClass(IClass cls,
                   ITestMethodFinder testMethodFinder,
                   IAnnotationFinder annotationFinder,
                   RunInfo runInfo,
                   XmlTest xmlTest,
                   XmlClass xmlClass) {
    init(cls, testMethodFinder, annotationFinder, runInfo, xmlTest, xmlClass);
  }

  /**
   * @return the name of this test if the class implements org.testng.ITest, null otherwise.
   */
  @Override
  public String getTestName() {
    return m_testName;
  }

  @Override
  public XmlTest getXmlTest() {
    return m_xmlTest;
  }

  @Override
  public XmlClass getXmlClass() {
    return m_xmlClass;
  }

  public IAnnotationFinder getAnnotationFinder() {
    return m_annotationFinder;
  }

  private void init(IClass cls,
                    ITestMethodFinder testMethodFinder,
                    IAnnotationFinder annotationFinder,
                    RunInfo runInfo,
                    XmlTest xmlTest,
                    XmlClass xmlClass)
  {
    log(3, "Creating TestClass for " + cls);
    m_iClass = cls;
    m_testClass = cls.getRealClass();
    m_xmlTest = xmlTest;
    m_xmlClass = xmlClass;
    m_runInfo = runInfo;
    m_testMethodFinder = testMethodFinder;
    m_annotationFinder = annotationFinder;
    initTestClassesAndInstances();
    initMethods();
  }

  private void initTestClassesAndInstances() {
    //
    // TestClasses and instances
    //
    Object[] instances = getInstances(false);
    for (Object instance : instances) {
      if (instance instanceof ITest) {
        m_testName = ((ITest) instance).getTestName();
        break;
      }
    }
    if (m_testName == null) {
      m_testName = m_iClass.getTestName();
    }
  }

  @Override
  public Object[] getInstances(boolean create) {
    return m_iClass.getInstances(create);
  }

  @Override
  public long[] getInstanceHashCodes() {
    return m_iClass.getInstanceHashCodes();
  }

  @Override
  public int getInstanceCount() {
    return m_iClass.getInstanceCount();
  }

  @Override
  public void addInstance(Object instance) {
    m_iClass.addInstance(instance);
  }

  private void initMethods() {
    ITestNGMethod[] methods = m_testMethodFinder.getTestMethods(m_testClass, m_xmlTest);
    m_testMethods = createTestMethods(methods);

    for (Object instance : m_iClass.getInstances(false)) {
      m_beforeSuiteMethods = ConfigurationMethod
          .createSuiteConfigurationMethods(m_testMethodFinder.getBeforeSuiteMethods(m_testClass),
                                           m_annotationFinder,
                                           true,
                                           instance);
      m_afterSuiteMethods = ConfigurationMethod
          .createSuiteConfigurationMethods(m_testMethodFinder.getAfterSuiteMethods(m_testClass),
                                           m_annotationFinder,
                                           false,
                                           instance);
      m_beforeTestConfMethods = ConfigurationMethod
          .createTestConfigurationMethods(m_testMethodFinder.getBeforeTestConfigurationMethods(m_testClass),
                                          m_annotationFinder,
                                          true,
                                          instance);
      m_afterTestConfMethods = ConfigurationMethod
          .createTestConfigurationMethods(m_testMethodFinder.getAfterTestConfigurationMethods(m_testClass),
                                          m_annotationFinder,
                                          false,
                                          instance);
      m_beforeClassMethods = ConfigurationMethod
          .createClassConfigurationMethods(m_testMethodFinder.getBeforeClassMethods(m_testClass),
                                           m_annotationFinder,
                                           true,
                                           instance);
      m_afterClassMethods = ConfigurationMethod
          .createClassConfigurationMethods(m_testMethodFinder.getAfterClassMethods(m_testClass),
                                           m_annotationFinder,
                                           false,
                                           instance);
      m_beforeGroupsMethods = ConfigurationMethod
          .createBeforeConfigurationMethods(m_testMethodFinder.getBeforeGroupsConfigurationMethods(m_testClass),
                                            m_annotationFinder,
                                            true,
                                            instance);
      m_afterGroupsMethods = ConfigurationMethod
          .createAfterConfigurationMethods(m_testMethodFinder.getAfterGroupsConfigurationMethods(m_testClass),
                                           m_annotationFinder,
                                           false,
                                           instance);
      m_beforeTestMethods = ConfigurationMethod
          .createTestMethodConfigurationMethods(m_testMethodFinder.getBeforeTestMethods(m_testClass),
                                                m_annotationFinder,
                                                true,
                                                instance);
      m_afterTestMethods = ConfigurationMethod
          .createTestMethodConfigurationMethods(m_testMethodFinder.getAfterTestMethods(m_testClass),
                                                m_annotationFinder,
                                                false,
                                                instance);
    }
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
        for (Object o : m_iClass.getInstances(false)) {
          log(4, "Adding method " + tm + " on TestClass " + m_testClass);
          vResult.add(new TestNGMethod(/* tm.getRealClass(), */ m, m_annotationFinder, m_xmlTest,
              o));
        }
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

  protected void dump() {
    System.out.println("===== Test class\n" + m_testClass.getName());
    for (ITestNGMethod m : m_beforeClassMethods) {
      System.out.println("  @BeforeClass " + m);
    }
    for (ITestNGMethod m : m_beforeTestMethods) {
      System.out.println("  @BeforeMethod " + m);
    }
    for (ITestNGMethod m : m_testMethods) {
      System.out.println("    @Test " + m);
    }
    for (ITestNGMethod m : m_afterTestMethods) {
      System.out.println("  @AfterMethod " + m);
    }
    for (ITestNGMethod m : m_afterClassMethods) {
      System.out.println("  @AfterClass " + m);
    }
    System.out.println("======");
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass())
        .add("name", m_testClass)
        .toString();
  }

  public IClass getIClass() {
    return m_iClass;
  }
}