package test.mannotation;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IParametersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.internal.IConfiguration;
import org.testng.internal.annotations.IAfterSuite;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IBeforeSuite;

import java.lang.reflect.Method;

@Test
public class MAnnotationSampleTest {
  private IConfiguration m_configuration = new org.testng.internal.Configuration();
  private IAnnotationFinder m_finder;

  @BeforeClass
  public void init() {
    m_finder = m_configuration.getAnnotationFinder();
  }

  public void verifyTestClassLevel() {
    //
    // Tests on MTest1SampleTest
    //
    ITestAnnotation test1 = m_finder.findAnnotation(MTest1.class, ITestAnnotation.class);
    Assert.assertTrue(test1.getEnabled());
    Assert.assertEquals(test1.getGroups(), new String[] { "group1", "group2" });
    Assert.assertTrue(test1.getAlwaysRun());
    Assert.assertEqualsNoOrder(test1.getDependsOnGroups(), new String[] { "dg1", "dg2" },  "depends on groups");
    Assert.assertEqualsNoOrder( test1.getDependsOnMethods(), new String[] { "dm1", "dm2" });
    Assert.assertEquals(test1.getTimeOut(), 42);
    Assert.assertEquals(test1.getInvocationCount(), 43);
    Assert.assertEquals(test1.getSuccessPercentage(), 44);
    Assert.assertEquals(test1.getThreadPoolSize(), 3);
    Assert.assertEquals(test1.getDataProvider(), "dp");
    Assert.assertEquals(test1.getDescription(), "Class level description");

    //
    // Tests on MTest1SampleTest (test defaults)
    //
    ITestAnnotation test2 = m_finder.findAnnotation(MTest2.class, ITestAnnotation.class);
    // test default for enabled
    Assert.assertTrue(test2.getEnabled());
    Assert.assertFalse(test2.getAlwaysRun());
    Assert.assertEquals(test2.getTimeOut(), 0);
    Assert.assertEquals(test2.getInvocationCount(), 1);
    Assert.assertEquals(test2.getSuccessPercentage(), 100);
    Assert.assertEquals(test2.getDataProvider(), "");
  }

  public void verifyTestMethodLevel() throws SecurityException, NoSuchMethodException
  {
    //
    // Tests on MTest1SampleTest
    //
    Method method = MTest1.class.getMethod("f");
    ITestAnnotation test1 = m_finder.findAnnotation(method, ITestAnnotation.class);
    Assert.assertTrue(test1.getEnabled());
    Assert.assertEqualsNoOrder(test1.getGroups(), new String[] { "group1", "group3", "group4", "group2" });
    Assert.assertTrue(test1.getAlwaysRun());
    Assert.assertEqualsNoOrder(test1.getDependsOnGroups(), new String[] { "dg1", "dg2", "dg3", "dg4" });
    Assert.assertEqualsNoOrder(test1.getDependsOnMethods(), new String[] { "dm1", "dm2", "dm3", "dm4" });
    Assert.assertEquals(test1.getTimeOut(), 142);
    Assert.assertEquals(test1.getInvocationCount(), 143);
    Assert.assertEquals(test1.getSuccessPercentage(), 61);
    Assert.assertEquals(test1.getDataProvider(), "dp2");
    Assert.assertEquals(test1.getDescription(), "Method description");
    Class[] exceptions = test1.getExpectedExceptions();
    Assert.assertEquals(exceptions.length, 1);
    Assert.assertEquals(exceptions[0], NullPointerException.class);
  }

  public void verifyDataProvider() throws SecurityException, NoSuchMethodException
  {
    Method method = MTest1.class.getMethod("otherConfigurations");
    IDataProviderAnnotation dataProvider = m_finder.findAnnotation(method, IDataProviderAnnotation.class);
    Assert.assertNotNull(dataProvider);
    Assert.assertEquals(dataProvider.getName(), "dp4");
  }

  public void verifyFactory() throws SecurityException, NoSuchMethodException
  {
    Method method = MTest1.class.getMethod("factory");
    IFactoryAnnotation factory= m_finder.findAnnotation(method, IFactoryAnnotation.class);

    Assert.assertNotNull(factory);
  }

  public void verifyParameters() throws SecurityException, NoSuchMethodException
  {
    Method method = MTest1.class.getMethod("parameters");
    IParametersAnnotation parameters = m_finder.findAnnotation(method, IParametersAnnotation.class);

    Assert.assertNotNull(parameters);
    Assert.assertEquals(parameters.getValue(), new String[] { "pp1", "pp2", "pp3" });
  }

  public void verifyNewConfigurationBefore() throws SecurityException, NoSuchMethodException
  {
    Method method = MTest1.class.getMethod("newBefore");
    IConfigurationAnnotation configuration =
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IBeforeSuite.class);
    Assert.assertNotNull(configuration);
    Assert.assertTrue(configuration.getBeforeSuite());

    // Default values
    Assert.assertTrue(configuration.getEnabled());
    Assert.assertTrue(configuration.getInheritGroups());
    Assert.assertFalse(configuration.getAlwaysRun());
  }

  public void verifyNewConfigurationAfter() throws SecurityException, NoSuchMethodException
  {
    Method method = MTest1.class.getMethod("newAfter");
    IConfigurationAnnotation configuration =
      (IConfigurationAnnotation) m_finder.findAnnotation(method, IAfterSuite.class);
    Assert.assertNotNull(configuration);
    Assert.assertTrue(configuration.getAfterSuite());

    // Default values
    Assert.assertTrue(configuration.getEnabled());
    Assert.assertTrue(configuration.getInheritGroups());
    Assert.assertFalse(configuration.getAlwaysRun());
  }

}
