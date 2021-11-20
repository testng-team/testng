package test.objectfactory;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.TestNGException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.objectfactory.github1131.EmptyConstructorSample;
import test.objectfactory.github1131.IntConstructorSample;
import test.objectfactory.github1131.MyObjectFactory;
import test.objectfactory.github1131.StringConstructorSample;
import test.objectfactory.github1827.GitHub1827Sample;
import test.objectfactory.issue2676.LocalSuiteAlteringListener;
import test.objectfactory.issue2676.LoggingObjectFactorySample;
import test.objectfactory.issue2676.TestClassSample;

public class ObjectFactoryTest extends SimpleBaseTest {

  @Test(description = "GITHUB-2676")
  public void ensureObjectFactoryIsInvokedWhenAddedViaListeners() {
    TestNG testng = create(TestClassSample.class);
    testng.addListener(new LocalSuiteAlteringListener());
    testng.run();
    assertThat(LoggingObjectFactorySample.wasInvoked).isTrue();
  }

  @Test(
      expectedExceptions = TestNGException.class,
      expectedExceptionsMessageRegExp = ".*Check to make sure it can be instantiated",
      description = "GITHUB-1827")
  public void ensureExceptionThrownWhenNoSuitableConstructorFound() {

    TestNG testng = create(GitHub1827Sample.class);
    testng.run();
  }

  @Test(dataProvider = "dp", description = "GITHUB-1131")
  public void factoryWithEmptyConstructorShouldWork(boolean bool) {
    testFactory(bool, EmptyConstructorSample.class);
    assertThat(MyObjectFactory.allParams).containsExactly(new Object[] {}, new Object[] {});
  }

  @Test(dataProvider = "dp", description = "GITHUB-1131")
  public void factoryWithIntConstructorShouldWork(boolean bool) {
    testFactory(bool, IntConstructorSample.class);
    assertThat(MyObjectFactory.allParams).containsExactly(new Object[] {1}, new Object[] {2});
  }

  @Test(dataProvider = "dp", description = "GITHUB-1131")
  public void factoryWithStringConstructorShouldWork(boolean bool) {
    testFactory(bool, StringConstructorSample.class);
    assertThat(MyObjectFactory.allParams)
        .containsExactly(new Object[] {"foo"}, new Object[] {"bar"});
  }

  private void testFactory(boolean onSuite, Class<?> sample) {
    MyObjectFactory.allParams.clear();

    XmlSuite suite = createXmlSuite("Test IObjectFactory2", "TmpTest", sample);
    TestNG tng = create(suite);

    if (onSuite) {
      suite.setObjectFactoryClass(MyObjectFactory.class);
    } else {
      tng.setObjectFactory(MyObjectFactory.class);
    }

    tng.run();
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {new Object[] {true}, new Object[] {false}};
  }
}
