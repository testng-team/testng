package org.testng.listeners.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.IDataProviderMethod;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.listeners.samples.github1490.DataProviderInfoProvider;
import org.testng.listeners.samples.github1490.FactoryPoweredDataProviderWithListenerAnnotationSample;
import org.testng.listeners.samples.github1490.FactoryPoweredDataProviderWithoutListenerAnnotationSample;
import org.testng.listeners.samples.github1490.InstanceAwareLocalDataProviderListener;
import org.testng.listeners.samples.github1490.InstanceBasedDataProviderWithListenerAnnotationSample;
import org.testng.listeners.samples.github1490.LocalDataProviderListener;
import org.testng.listeners.samples.github1490.SimpleDataProviderWithListenerAnnotationSample;
import org.testng.listeners.samples.github1490.SimpleDataProviderWithListenerAnnotationSample1;
import org.testng.listeners.samples.github1490.SimpleDataProviderWithoutListenerAnnotationSample;
import org.testng.listeners.samples.github1490.StaticDataProviderWithListenerAnnotationSample;
import org.testng.listeners.samples.github1490.TwoFactoriesShareSameDataProviderSampleOne;
import org.testng.listeners.samples.github1490.TwoFactoriesShareSameDataProviderSampleTwo;
import org.testng.listeners.samples.github1490.TwoTestMethodsShareSameDataProviderSample;
import org.testng.listeners.samples.github1490.TwoTestMethodsShareSameDataProviderSampleTwo;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class VerifyDataProviderListener extends SimpleBaseTest {

  @Test(description = "GITHUB-1490")
  public void testInstanceBasedDataProviderInformation() {
    TestNG tng = create(InstanceBasedDataProviderWithListenerAnnotationSample.class);
    tng.run();
    IDataProviderMethod before = DataProviderInfoProvider.before;
    IDataProviderMethod after = DataProviderInfoProvider.after;
    assertThat(before).isEqualTo(after);
    assertThat(before.getInstance()).isEqualTo(after.getInstance());
    assertThat(before.getMethod().getName()).isEqualTo("getData");
  }

  @Test(description = "GITHUB-1490")
  public void testStaticDataProviderInformation() {
    TestNG tng = create(StaticDataProviderWithListenerAnnotationSample.class);
    tng.run();
    IDataProviderMethod before = DataProviderInfoProvider.before;
    IDataProviderMethod after = DataProviderInfoProvider.after;
    assertThat(before).isEqualTo(after);
    assertThat(before.getInstance()).isNull();
    assertThat(before.getMethod().getName()).isEqualTo("getStaticData");
  }

  @Test(description = "GITHUB-1490")
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

  @Test(description = "GITHUB-1490")
  public void testMultipleFactoriesShareSameDataProvider() {
    Class<?>[] classes = {
      TwoFactoriesShareSameDataProviderSampleOne.class,
      TwoFactoriesShareSameDataProviderSampleTwo.class
    };
    runTest(0, classes);
  }

  @Test(description = "GITHUB-1490")
  public void testMultipleMethodsFactoriesShareSampleDataProvider() {
    Class<?>[] classes = {
      TwoFactoriesShareSameDataProviderSampleOne.class,
      TwoFactoriesShareSameDataProviderSampleTwo.class,
      TwoTestMethodsShareSameDataProviderSampleTwo.class
    };
    runTest(1, classes);
  }

  @Test(description = "GITHUB-1490")
  public void testSimpleDataProviderWithListenerAnnotation() {
    final String prefix =
        ":" + SimpleDataProviderWithListenerAnnotationSample.class.getName() + ".testMethod";
    runTest(prefix, SimpleDataProviderWithListenerAnnotationSample.class, true);
  }

  @Test(description = "GITHUB-1490")
  public void testFactoryPoweredDataProviderWithListenerAnnotation() {
    final String prefix =
        ":" + FactoryPoweredDataProviderWithListenerAnnotationSample.class.getName();
    runTest(prefix, FactoryPoweredDataProviderWithListenerAnnotationSample.class, true);
  }

  @Test(description = "GITHUB-1490")
  public void testSimpleDataProviderWithoutListenerAnnotation() {
    final String prefix =
        ":" + SimpleDataProviderWithoutListenerAnnotationSample.class.getName() + ".testMethod";
    runTest(prefix, SimpleDataProviderWithoutListenerAnnotationSample.class, false);
  }

  @Test(description = "GITHUB-1490")
  public void testFactoryPoweredDataProviderWithoutListenerAnnotation() {
    final String prefix =
        ":" + FactoryPoweredDataProviderWithoutListenerAnnotationSample.class.getName();
    runTest(prefix, FactoryPoweredDataProviderWithoutListenerAnnotationSample.class, false);
  }

  @Test(description = "GITHUB-1490")
  public void testSimpleDataProviderWithListenerViaSuiteXml() {
    final String prefix =
        ":" + SimpleDataProviderWithoutListenerAnnotationSample.class.getName() + ".testMethod";
    runTestWithListenerViaSuiteXml(prefix, SimpleDataProviderWithoutListenerAnnotationSample.class);
  }

  @Test(description = "GITHUB-1490")
  public void testFactoryPoweredDataProviderWithListenerViaSuiteXml() {
    final String prefix =
        ":" + FactoryPoweredDataProviderWithoutListenerAnnotationSample.class.getName();
    runTestWithListenerViaSuiteXml(
        prefix, FactoryPoweredDataProviderWithoutListenerAnnotationSample.class);
  }

  @Test(description = "GITHUB-1490")
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
