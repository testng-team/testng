package test.mannotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
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
    assertThat(test1.getEnabled()).isTrue();
    assertThat(test1.getGroups()).isEqualTo(new String[] {"group1", "group2"});
    assertThat(test1.getAlwaysRun()).isTrue();
    assertThat(test1.getDependsOnGroups())
        .withFailMessage("depends on groups")
        .containsExactlyInAnyOrder(new String[] {"dg1", "dg2"});
    assertThat(test1.getDependsOnMethods()).containsExactlyInAnyOrder(new String[] {"dm1", "dm2"});
    assertThat(test1.getTimeOut()).isEqualTo(42);
    assertThat(test1.getInvocationCount()).isEqualTo(43);
    assertThat(test1.getSuccessPercentage()).isEqualTo(44);
    assertThat(test1.getThreadPoolSize()).isEqualTo(3);
    assertThat(test1.getDataProvider()).isEqualTo("dp");
    assertThat(test1.getDescription()).isEqualTo("Class level description");

    //
    // Tests on MTest1SampleTest (test defaults)
    //
    ITestAnnotation test2 = m_finder.findAnnotation(MTest2.class, ITestAnnotation.class);
    // test default for enabled
    assertThat(test2.getEnabled()).isTrue();
    assertThat(test2.getAlwaysRun()).isFalse();
    assertThat(test2.getTimeOut()).isZero();
    assertThat(test2.getInvocationCount()).isOne();
    assertThat(test2.getSuccessPercentage()).isEqualTo(100);
    assertThat(test2.getDataProvider()).isEmpty();
  }

  public void verifyTestMethodLevel() throws SecurityException, NoSuchMethodException {
    //
    // Tests on MTest1SampleTest
    //
    Method method = MTest1.class.getMethod("f");
    ITestAnnotation test1 = m_finder.findAnnotation(method, ITestAnnotation.class);
    assertThat(test1.getEnabled()).isTrue();
    assertThat(test1.getGroups())
        .containsExactlyInAnyOrder(new String[] {"group1", "group3", "group4", "group2"});
    assertThat(test1.getAlwaysRun()).isTrue();
    assertThat(test1.getDependsOnGroups())
        .containsExactlyInAnyOrder(new String[] {"dg1", "dg2", "dg3", "dg4"});
    assertThat(test1.getDependsOnMethods())
        .containsExactlyInAnyOrder(new String[] {"dm1", "dm2", "dm3", "dm4"});
    assertThat(test1.getTimeOut()).isEqualTo(142);
    assertThat(test1.getInvocationCount()).isEqualTo(143);
    assertThat(test1.getSuccessPercentage()).isEqualTo(61);
    assertThat(test1.getDataProvider()).isEqualTo("dp2");
    assertThat(test1.getDescription()).isEqualTo("Method description");
    Class[] exceptions = test1.getExpectedExceptions();
    assertThat(exceptions.length).isOne();
    assertThat(exceptions[0]).isEqualTo(NullPointerException.class);
  }

  public void verifyDataProvider() throws SecurityException, NoSuchMethodException {
    Method method = MTest1.class.getMethod("otherConfigurations");
    IDataProviderAnnotation dataProvider =
        m_finder.findAnnotation(method, IDataProviderAnnotation.class);
    assertThat(dataProvider).isNotNull();
    assertThat(dataProvider.getName()).isEqualTo("dp4");
  }

  public void verifyFactory() throws SecurityException, NoSuchMethodException {
    Method method = MTest1.class.getMethod("factory");
    IFactoryAnnotation factory = m_finder.findAnnotation(method, IFactoryAnnotation.class);

    assertThat(factory).isNotNull();
  }

  public void verifyParameters() throws SecurityException, NoSuchMethodException {
    Method method = MTest1.class.getMethod("parameters");
    IParametersAnnotation parameters = m_finder.findAnnotation(method, IParametersAnnotation.class);

    assertThat(parameters).isNotNull();
    assertThat(parameters.getValue()).isEqualTo(new String[] {"pp1", "pp2", "pp3"});
  }

  public void verifyNewConfigurationBefore() throws SecurityException, NoSuchMethodException {
    Method method = MTest1.class.getMethod("newBefore");
    IConfigurationAnnotation configuration =
        (IConfigurationAnnotation) m_finder.findAnnotation(method, IBeforeSuite.class);
    assertThat(configuration).isNotNull();
    assertThat(configuration.getBeforeSuite()).isTrue();

    // Default values
    assertThat(configuration.getEnabled()).isTrue();
    assertThat(configuration.getInheritGroups()).isTrue();
    assertThat(configuration.getAlwaysRun()).isFalse();
  }

  public void verifyNewConfigurationAfter() throws SecurityException, NoSuchMethodException {
    Method method = MTest1.class.getMethod("newAfter");
    IConfigurationAnnotation configuration =
        (IConfigurationAnnotation) m_finder.findAnnotation(method, IAfterSuite.class);
    assertThat(configuration).isNotNull();
    assertThat(configuration.getAfterSuite()).isTrue();

    // Default values
    assertThat(configuration.getEnabled()).isTrue();
    assertThat(configuration.getInheritGroups()).isTrue();
    assertThat(configuration.getAlwaysRun()).isFalse();
  }
}
