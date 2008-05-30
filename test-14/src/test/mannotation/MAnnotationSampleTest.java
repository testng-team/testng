package test.mannotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IExpectedExceptionsAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAfterSuite;
import org.testng.internal.annotations.IBeforeSuite;
import org.testng.internal.annotations.JDK14AnnotationFinder;

/**
 * @testng.test
 */
public class MAnnotationSampleTest {
  private JDK14AnnotationFinder m_finder;
  
  /**
   * @testng.before-class description = "New annotation"
   */
  public void init() {
    m_finder = new JDK14AnnotationFinder(new DefaultAnnotationTransformer());
    m_finder.addSourceDirs(new String[] {
        "./test-14/src",
        "src"
      });
  }

  public void verifyTestClassLevel() {
    //
    // Tests on MTest1SampleTest
    //
    ITestAnnotation test1 = (ITestAnnotation) m_finder.findAnnotation(MTest1.class, ITestAnnotation.class);
    Assert.assertTrue(test1.getEnabled());
    String[] groups = test1.getGroups();
    Assert.assertEquals(new String[] { "group1", "group2" }, groups);
    Assert.assertTrue(test1.getAlwaysRun());
    Assert.assertEquals(new String[] { "param1", "param2" }, test1.getParameters());
    Assert.assertEquals(new String[] { "dg1", "dg2" }, test1.getDependsOnGroups());
    Assert.assertEquals(new String[] { "dm1", "dm2" }, test1.getDependsOnMethods());
    Assert.assertEquals(42, test1.getTimeOut());
    Assert.assertEquals(43, test1.getInvocationCount());
    Assert.assertEquals(44, test1.getSuccessPercentage());
    Assert.assertEquals("dp", test1.getDataProvider());
    Assert.assertEquals("Class level description", test1.getDescription());
    
    //
    // Tests on MTest1SampleTest (test defaults)
    //
    ITestAnnotation test2 = (ITestAnnotation) m_finder.findAnnotation(MTest2.class, ITestAnnotation.class);
    // test default for enabled
    Assert.assertTrue(test2.getEnabled());
    Assert.assertFalse(test2.getAlwaysRun());
    Assert.assertEquals(0, test2.getTimeOut());
    Assert.assertEquals(1, test2.getInvocationCount());
    Assert.assertEquals(100, test2.getSuccessPercentage());
    Assert.assertEquals("", test2.getDataProvider());
  }
  
  public void verifyTestMethodLevel() 
    throws SecurityException, NoSuchMethodException 
  {
    //
    // Tests on MTest1SampleTest
    //
    Method method = MTest1.class.getMethod("f", null);
    ITestAnnotation test1 = (ITestAnnotation) m_finder.findAnnotation(method, ITestAnnotation.class);
    Assert.assertTrue(test1.getEnabled());
    String[] groups = test1.getGroups();
    Assert.assertEquals(new String[] { "group3", "group4" }, groups);
    Assert.assertTrue(test1.getAlwaysRun());
    Assert.assertEquals(new String[] { "param3", "param4" }, test1.getParameters());
    Assert.assertEquals(new String[] { "dg3", "dg4" }, test1.getDependsOnGroups());
    Assert.assertEquals(new String[] { "dm3", "dm4" }, test1.getDependsOnMethods());
    Assert.assertEquals(142, test1.getTimeOut());
    Assert.assertEquals(143, test1.getInvocationCount());
    Assert.assertEquals(3, test1.getThreadPoolSize());
    Assert.assertEquals(61, test1.getSuccessPercentage());
    Assert.assertEquals("dp2", test1.getDataProvider());
    Assert.assertEquals("Method description", test1.getDescription());
    Class[] exceptions = test1.getExpectedExceptions();
    Assert.assertEquals(exceptions.length, 1);
    Assert.assertEquals(exceptions[0], NullPointerException.class);
  }  
  
  public void verifyTestConstructorLevel() 
    throws SecurityException, NoSuchMethodException 
  {
    //
    // Tests on MTest1SampleTest
    //
    Constructor constructor = MTest1.class.getConstructor( null);
    ITestAnnotation test1 = (ITestAnnotation) m_finder.findAnnotation(constructor, ITestAnnotation.class);
    Assert.assertNotNull(test1);
    Assert.assertTrue(test1.getEnabled());
    String[] groups = test1.getGroups();
    Assert.assertEquals(new String[] { "group5", "group6" }, groups);
    Assert.assertTrue(test1.getAlwaysRun());
    Assert.assertEquals(new String[] { "param5", "param6" }, test1.getParameters());
    Assert.assertEquals(new String[] { "dg5", "dg6" }, test1.getDependsOnGroups());
    Assert.assertEquals(new String[] { "dm5", "dm6" }, test1.getDependsOnMethods());
    Assert.assertEquals(242, test1.getTimeOut());
    Assert.assertEquals(243, test1.getInvocationCount());
    Assert.assertEquals(62, test1.getSuccessPercentage());
    Assert.assertEquals("dp3", test1.getDataProvider());
    Assert.assertEquals("Constructor description", test1.getDescription());
    Class[] exceptions = test1.getExpectedExceptions();
    Assert.assertEquals(exceptions.length, 1);
    Assert.assertEquals(exceptions[0], NullPointerException.class);
  }  
  
  public void verifyConfigurationBefore() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("before", null);
    IConfigurationAnnotation configuration = 
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IConfigurationAnnotation.class);
    Assert.assertNotNull(configuration);
    Assert.assertTrue(configuration.getBeforeSuite());
    Assert.assertTrue(configuration.getBeforeTestMethod());
    Assert.assertTrue(configuration.getBeforeTest());
    Assert.assertTrue(configuration.getBeforeTestClass());
    Assert.assertFalse(configuration.getAfterSuite());
    Assert.assertFalse(configuration.getAfterTestMethod());
    Assert.assertFalse(configuration.getAfterTest());
    Assert.assertFalse(configuration.getAfterTestClass());
    Assert.assertEquals(0, configuration.getAfterGroups().length);
    String[] bg = configuration.getBeforeGroups();
    Assert.assertEquals(bg.length, 2);
    Assert.assertTrue((bg[0].equals("b1") && bg[1].equals("b2"))
        || (bg[1].equals("b1") && bg[0].equals("b2")));
    
    // Default values
    Assert.assertTrue(configuration.getEnabled());
    Assert.assertTrue(configuration.getInheritGroups());
    Assert.assertFalse(configuration.getAlwaysRun());
  }
  
  public void verifyNewConfigurationBefore() 
  throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("newBefore", new Class[0]);
    IConfigurationAnnotation configuration = 
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IBeforeSuite.class);
    Assert.assertNotNull(configuration);
    Assert.assertTrue(configuration.getBeforeSuite());
    
    // Default values
    Assert.assertTrue(configuration.getEnabled());
    Assert.assertTrue(configuration.getInheritGroups());
    Assert.assertFalse(configuration.getAlwaysRun());
  }

  public void verifyNewConfigurationAfter() 
  throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("newAfter", new Class[0]);
    IConfigurationAnnotation configuration = 
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IAfterSuite.class);
    Assert.assertNotNull(configuration);
    Assert.assertTrue(configuration.getAfterSuite());

    // Default values
    Assert.assertTrue(configuration.getEnabled());
    Assert.assertTrue(configuration.getInheritGroups());
    Assert.assertFalse(configuration.getAlwaysRun());
  }

  public void verifyConfigurationAfter() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("after", null);
    IConfigurationAnnotation configuration = 
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IConfigurationAnnotation.class);
    Assert.assertNotNull(configuration);
    Assert.assertFalse(configuration.getBeforeSuite());
    Assert.assertFalse(configuration.getBeforeTestMethod());
    Assert.assertFalse(configuration.getBeforeTest());
    Assert.assertFalse(configuration.getBeforeTestClass());
    Assert.assertTrue(configuration.getAfterSuite());
    Assert.assertTrue(configuration.getAfterTestMethod());
    Assert.assertTrue(configuration.getAfterTest());
    Assert.assertTrue(configuration.getAfterTestClass());
    Assert.assertEquals(0, configuration.getBeforeGroups().length);
    String[] ag = configuration.getAfterGroups();
    Assert.assertEquals(ag.length, 2);
    Assert.assertTrue((ag[0].equals("a1") && ag[1].equals("a2"))
        || (ag[1].equals("a1") && ag[0].equals("a2")));
    
    // Default values
    Assert.assertTrue(configuration.getEnabled());
    Assert.assertTrue(configuration.getInheritGroups());
    Assert.assertFalse(configuration.getAlwaysRun());
  }  
  
  public void verifyConfigurationOthers() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("otherConfigurations", null);
    IConfigurationAnnotation configuration = 
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IConfigurationAnnotation.class);
    Assert.assertNotNull(configuration);
    Assert.assertFalse(configuration.getBeforeSuite());
    Assert.assertFalse(configuration.getBeforeTestMethod());
    Assert.assertFalse(configuration.getBeforeTest());
    Assert.assertFalse(configuration.getBeforeTestClass());
    Assert.assertFalse(configuration.getAfterSuite());
    Assert.assertFalse(configuration.getAfterTestMethod());
    Assert.assertFalse(configuration.getAfterTest());
    Assert.assertFalse(configuration.getAfterTestClass());
    
    Assert.assertFalse(configuration.getEnabled());
    Assert.assertEquals(new String[] { "oparam1", "oparam2" }, configuration.getParameters());
    Assert.assertEquals(new String[] { "ogroup1", "ogroup2" }, configuration.getGroups());
    Assert.assertEquals(new String[] { "odg1", "odg2" }, configuration.getDependsOnGroups());
    Assert.assertEquals(new String[] { "odm1", "odm2" }, configuration.getDependsOnMethods());
    Assert.assertFalse(configuration.getInheritGroups());
    Assert.assertTrue(configuration.getAlwaysRun());
    Assert.assertEquals(configuration.getDescription(), "beforeSuite description");
    
  }  
  
  public void verifygetDataProvider() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("otherConfigurations", null);
    IDataProviderAnnotation dataProvider = 
      (IDataProviderAnnotation) m_finder.findAnnotation(method, IDataProviderAnnotation.class);
    Assert.assertNotNull(dataProvider);
    Assert.assertEquals("dp4", dataProvider.getName());
  }  
  
  public void verifyExpectedExceptions() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("otherConfigurations", null);
    IExpectedExceptionsAnnotation exceptions= 
      (IExpectedExceptionsAnnotation) m_finder.findAnnotation(method, IExpectedExceptionsAnnotation.class);
    
    Assert.assertNotNull(exceptions);
    Assert.assertEquals(new Class[] { MTest1.class, MTest2.class }, exceptions.getValue());
  }

  public void verifyFactory() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("factory", null);
    IFactoryAnnotation factory= 
      (IFactoryAnnotation) m_finder.findAnnotation(method, IFactoryAnnotation.class);
    
    Assert.assertNotNull(factory);
    Assert.assertEquals(new String[] { "pf1", "pf2" }, factory.getParameters());
  }

  public void verifyGetParameters() 
    throws SecurityException, NoSuchMethodException 
  {
    Method method = MTest1.class.getMethod("parameters", null);
    IParametersAnnotation parameters = 
      (IParametersAnnotation) m_finder.findAnnotation(method, IParametersAnnotation.class);
    
    Assert.assertNotNull(parameters);
    Assert.assertEquals(new String[] { "pp1", "pp2", "pp3" }, parameters.getValue());
  }

  private static void ppp(String s) {
    System.out.println("[MAnnotationSampleTest] " + s);
  }
}
