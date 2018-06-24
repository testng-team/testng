package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.Assert;
import org.testng.IDataProviderMethod;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.listeners.github1490.DataProviderInfoProvider;
import test.listeners.github1490.InstanceAwareLocalDataProviderListener;
import test.listeners.github1490.LocalDataProviderListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerifyDataProviderListener extends SimpleBaseTest {

  @Test
  public void testInstanceBasedDataProviderInformation() {
    TestNG tng = create(InstanceBasedDataProviderWithListenerAnnotationSample.class);
    tng.run();
    IDataProviderMethod before = DataProviderInfoProvider.before;
    IDataProviderMethod after = DataProviderInfoProvider.after;
    Assert.assertEquals(before, after);
    Assert.assertEquals(before.getInstance(), after.getInstance());
    Assert.assertEquals(before.getMethod().getName(), "getData");
  }

  @Test
  public void testStaticDataProviderInformation() {
    TestNG tng = create(StaticDataProviderWithListenerAnnotationSample.class);
    tng.run();
    IDataProviderMethod before = DataProviderInfoProvider.before;
    IDataProviderMethod after = DataProviderInfoProvider.after;
    Assert.assertEquals(before, after);
    Assert.assertNull(before.getInstance());
    Assert.assertEquals(before.getMethod().getName(), "getStaticData");
  }

  @Test
  public void testMultipleTestMethodsShareSameDataProvider() {
    Class<?> clazz = TwoTestMethodsShareSameDataProviderSample.class;
    runTest(1, clazz);
    String[] prefixes = {"before", "after"};
    String[] methods = {"testHowMuchMasterShifuAte", "testHowMuchPoAte"};
    List<String> expected = new ArrayList<>();
    for (String prefix : prefixes) {
      for (String method : methods) {
        String txt = prefix + ":" + clazz.getName() + "." + method;
        expected.add(txt);
      }
    }
    assertThat(InstanceAwareLocalDataProviderListener.messages).containsAll(expected);
  }

  @Test
  public void testMultipleFactoriesShareSameDataProvider() {
    Class<?>[] classes = {
      TwoFactoriesShareSameDataProviderSampleOne.class,
      TwoFactoriesShareSameDataProviderSampleTwo.class
    };
    runTest(0, classes);
  }

  @Test
  public void testMultipleMethodsFactoriesShareSampleDataProvider() {
    Class<?>[] classes = {
      TwoFactoriesShareSameDataProviderSampleOne.class,
      TwoFactoriesShareSameDataProviderSampleTwo.class,
      TwoTestMethodsShareSameDataProviderSampleTwo.class
    };
    runTest(1, classes);
  }

  @Test
  public void testSimpleDataProviderWithListenerAnnotation() {
    final String prefix =
        ":" + SimpleDataProviderWithListenerAnnotationSample.class.getName() + ".testMethod";
    runTest(prefix, SimpleDataProviderWithListenerAnnotationSample.class, true);
  }

  @Test
  public void testFactoryPoweredDataProviderWithListenerAnnotation() {
    final String prefix =
        ":" + FactoryPoweredDataProviderWithListenerAnnotationSample.class.getName();
    runTest(prefix, FactoryPoweredDataProviderWithListenerAnnotationSample.class, true);
  }

  @Test
  public void testSimpleDataProviderWithoutListenerAnnotation() {
    final String prefix =
        ":" + SimpleDataProviderWithoutListenerAnnotationSample.class.getName() + ".testMethod";
    runTest(prefix, SimpleDataProviderWithoutListenerAnnotationSample.class, false);
  }

  @Test
  public void testFactoryPoweredDataProviderWithoutListenerAnnotation() {
    final String prefix =
        ":" + FactoryPoweredDataProviderWithoutListenerAnnotationSample.class.getName();
    runTest(prefix, FactoryPoweredDataProviderWithoutListenerAnnotationSample.class, false);
  }

  @Test
  public void testSimpleDataProviderWithListenerViaSuiteXml() {
    final String prefix =
        ":" + SimpleDataProviderWithoutListenerAnnotationSample.class.getName() + ".testMethod";
    runTestWithListenerViaSuiteXml(prefix, SimpleDataProviderWithoutListenerAnnotationSample.class);
  }

  @Test
  public void testFactoryPoweredDataProviderWithListenerViaSuiteXml() {
    final String prefix =
        ":" + FactoryPoweredDataProviderWithoutListenerAnnotationSample.class.getName();
    runTestWithListenerViaSuiteXml(
        prefix, FactoryPoweredDataProviderWithoutListenerAnnotationSample.class);
  }

  @Test
  public void testSimpleDataProviderWithListenerAnnotationAndInvolvingInheritance() {
    final String prefix =
        ":" + SimpleDataProviderWithListenerAnnotationSample1.class.getName() + ".testMethod";
    TestNG tng = create(SimpleDataProviderWithListenerAnnotationSample1.class);
    tng.run();
    assertThat(LocalDataProviderListener.messages)
        .containsExactlyElementsOf(Arrays.asList("before" + prefix, "after" + prefix));
  }

  @AfterMethod
  public void resetListenerMessages() {
    LocalDataProviderListener.messages.clear();
  }

  private static void runTestWithListenerViaSuiteXml(String prefix, Class<?> clazz) {
    XmlSuite xmlSuite = createXmlSuite("SampleSuite");
    XmlTest xmlTest = createXmlTest(xmlSuite, "SampleTest");
    createXmlClass(xmlTest, clazz);
    xmlSuite.addListener(LocalDataProviderListener.class.getName());
    TestNG tng = create(xmlSuite);
    tng.run();
    assertThat(LocalDataProviderListener.messages)
        .containsExactlyElementsOf(Arrays.asList("before" + prefix, "after" + prefix));
  }

  private static void runTest(String prefix, Class<?> clazz, boolean hasListenerAnnotation) {
    TestNG tng = create(clazz);
    if (!hasListenerAnnotation) {
      tng.addListener(new LocalDataProviderListener());
    }
    tng.run();
    assertThat(LocalDataProviderListener.messages)
        .containsExactlyElementsOf(Arrays.asList("before" + prefix, "after" + prefix));
  }

  private static void runTest(int expected, Class<?>... classes) {
    TestNG tng = create(classes);
    tng.addListener(new InstanceAwareLocalDataProviderListener());
    tng.run();
    assertThat(InstanceAwareLocalDataProviderListener.instanceCollectionBeforeExecution)
        .size()
        .isEqualTo(expected);
    assertThat(InstanceAwareLocalDataProviderListener.instanceCollectionAfterExecution)
        .size()
        .isEqualTo(expected);
  }
}
