package org.testng.internal;

import java.util.Arrays;
import java.util.List;
import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Local measurement for {@link ConfigurationMethod#bind(IObject.IdentifiableObject)} on a class
 * carrying one method of every configuration type, at the 5000-instance scale from GITHUB-3436.
 *
 * <p>Not part of CI — run after {@code ./gradlew :testng-core:testClasses} with the test runtime
 * classpath.
 */
public final class ConfigurationMethodBindMeasurement {

  private static final int INSTANCES = 5000;
  private static final int WARMUP = 3;
  private static final int RUNS = 5;

  public static void main(String[] args) {
    FactoryConfigurationPrototypes prototypes = FactoryConfigurationPrototypes.build();
    IObject.IdentifiableObject[] instances = new IObject.IdentifiableObject[INSTANCES];
    for (int i = 0; i < INSTANCES; i++) {
      instances[i] = new IObject.IdentifiableObject(new FactoryConfigurationSample());
    }

    long bindNanos = medianNanos(() -> bindAll(prototypes, instances));
    long initNanos = medianNanos(() -> initAll(prototypes.templates, instances));

    System.out.printf(
        "ConfigurationMethod bind vs fresh init() at %d instances (median of %d runs, ns):%n",
        INSTANCES, RUNS);
    System.out.printf("  bind():              %,d ns (~%.2f ms)%n", bindNanos, bindNanos / 1e6);
    System.out.printf(
        "  new ConfigurationMethod(..., init): %,d ns (~%.2f ms)%n", initNanos, initNanos / 1e6);
    System.out.printf("  ratio (init/bind):   %.1fx%n", (double) initNanos / bindNanos);
  }

  private static long medianNanos(Runnable work) {
    long[] samples = new long[RUNS];
    for (int i = 0; i < WARMUP; i++) {
      work.run();
    }
    for (int i = 0; i < RUNS; i++) {
      long start = System.nanoTime();
      work.run();
      samples[i] = System.nanoTime() - start;
    }
    Arrays.sort(samples);
    return samples[RUNS / 2];
  }

  private static void bindAll(
      FactoryConfigurationPrototypes prototypes, IObject.IdentifiableObject[] instances) {
    for (IObject.IdentifiableObject each : instances) {
      ConfigurationMethod.bind(prototypes.beforeSuite, each);
      ConfigurationMethod.bind(prototypes.afterSuite, each);
      ConfigurationMethod.bind(prototypes.beforeTest, each);
      ConfigurationMethod.bind(prototypes.afterTest, each);
      ConfigurationMethod.bind(prototypes.beforeClass, each);
      ConfigurationMethod.bind(prototypes.afterClass, each);
      ConfigurationMethod.bind(prototypes.beforeGroups, each);
      ConfigurationMethod.bind(prototypes.afterGroups, each);
      ConfigurationMethod.bind(prototypes.beforeMethod, each);
      ConfigurationMethod.bind(prototypes.afterMethod, each);
    }
  }

  private static void initAll(
      ConfigurationTemplates templates, IObject.IdentifiableObject[] instances) {
    for (IObject.IdentifiableObject each : instances) {
      createSuite(templates.beforeSuite, true, each);
      createSuite(templates.afterSuite, false, each);
      createTest(templates.beforeTest, true, each);
      createTest(templates.afterTest, false, each);
      createClass(templates.beforeClass, true, each);
      createClass(templates.afterClass, false, each);
      createBeforeGroups(templates.beforeGroups, each);
      createAfterGroups(templates.afterGroups, each);
      createTestMethod(templates.beforeMethod, true, each);
      createTestMethod(templates.afterMethod, false, each);
    }
  }

  private static List<ITestNGMethod> createSuite(
      ITestNGMethod[] templates, boolean isBefore, IObject.IdentifiableObject instance) {
    return ConfigurationMethod.createSuiteConfigurationMethods(
        OBJECT_FACTORY, templates, ANNOTATION_FINDER, isBefore, instance);
  }

  private static List<ITestNGMethod> createTest(
      ITestNGMethod[] templates, boolean isBefore, IObject.IdentifiableObject instance) {
    return ConfigurationMethod.createTestConfigurationMethods(
        OBJECT_FACTORY, templates, ANNOTATION_FINDER, isBefore, XML_TEST, instance);
  }

  private static List<ITestNGMethod> createClass(
      ITestNGMethod[] templates, boolean isBefore, IObject.IdentifiableObject instance) {
    return ConfigurationMethod.createClassConfigurationMethods(
        OBJECT_FACTORY, templates, ANNOTATION_FINDER, isBefore, XML_TEST, instance);
  }

  private static ITestNGMethod[] createBeforeGroups(
      ITestNGMethod[] templates, IObject.IdentifiableObject instance) {
    return ConfigurationMethod.createBeforeConfigurationMethods(
        OBJECT_FACTORY, templates, ANNOTATION_FINDER, true, instance);
  }

  private static List<ITestNGMethod> createAfterGroups(
      ITestNGMethod[] templates, IObject.IdentifiableObject instance) {
    return ConfigurationMethod.createAfterConfigurationMethods(
        OBJECT_FACTORY, templates, ANNOTATION_FINDER, false, instance);
  }

  private static List<ITestNGMethod> createTestMethod(
      ITestNGMethod[] templates, boolean isBefore, IObject.IdentifiableObject instance) {
    return ConfigurationMethod.createTestMethodConfigurationMethods(
        OBJECT_FACTORY, templates, ANNOTATION_FINDER, isBefore, XML_TEST, instance);
  }

  private static final DefaultTestObjectFactory OBJECT_FACTORY = new DefaultTestObjectFactory();
  private static final IAnnotationFinder ANNOTATION_FINDER =
      new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
  private static final XmlSuite XML_SUITE = new XmlSuite();
  private static final XmlTest XML_TEST = new XmlTest(XML_SUITE);
  private static final XmlClass XML_CLASS =
      new XmlClass(FactoryConfigurationSample.class.getName());

  static {
    XML_TEST.getXmlClasses().add(XML_CLASS);
  }

  private static final class ConfigurationTemplates {
    final ITestNGMethod[] beforeSuite;
    final ITestNGMethod[] afterSuite;
    final ITestNGMethod[] beforeTest;
    final ITestNGMethod[] afterTest;
    final ITestNGMethod[] beforeClass;
    final ITestNGMethod[] afterClass;
    final ITestNGMethod[] beforeGroups;
    final ITestNGMethod[] afterGroups;
    final ITestNGMethod[] beforeMethod;
    final ITestNGMethod[] afterMethod;

    ConfigurationTemplates(
        ITestNGMethod[] beforeSuite,
        ITestNGMethod[] afterSuite,
        ITestNGMethod[] beforeTest,
        ITestNGMethod[] afterTest,
        ITestNGMethod[] beforeClass,
        ITestNGMethod[] afterClass,
        ITestNGMethod[] beforeGroups,
        ITestNGMethod[] afterGroups,
        ITestNGMethod[] beforeMethod,
        ITestNGMethod[] afterMethod) {
      this.beforeSuite = beforeSuite;
      this.afterSuite = afterSuite;
      this.beforeTest = beforeTest;
      this.afterTest = afterTest;
      this.beforeClass = beforeClass;
      this.afterClass = afterClass;
      this.beforeGroups = beforeGroups;
      this.afterGroups = afterGroups;
      this.beforeMethod = beforeMethod;
      this.afterMethod = afterMethod;
    }
  }

  private static final class FactoryConfigurationPrototypes {
    final List<ITestNGMethod> beforeSuite;
    final List<ITestNGMethod> afterSuite;
    final List<ITestNGMethod> beforeTest;
    final List<ITestNGMethod> afterTest;
    final List<ITestNGMethod> beforeClass;
    final List<ITestNGMethod> afterClass;
    final ITestNGMethod[] beforeGroups;
    final List<ITestNGMethod> afterGroups;
    final List<ITestNGMethod> beforeMethod;
    final List<ITestNGMethod> afterMethod;
    final ConfigurationTemplates templates;

    FactoryConfigurationPrototypes(
        List<ITestNGMethod> beforeSuite,
        List<ITestNGMethod> afterSuite,
        List<ITestNGMethod> beforeTest,
        List<ITestNGMethod> afterTest,
        List<ITestNGMethod> beforeClass,
        List<ITestNGMethod> afterClass,
        ITestNGMethod[] beforeGroups,
        List<ITestNGMethod> afterGroups,
        List<ITestNGMethod> beforeMethod,
        List<ITestNGMethod> afterMethod,
        ConfigurationTemplates templates) {
      this.beforeSuite = beforeSuite;
      this.afterSuite = afterSuite;
      this.beforeTest = beforeTest;
      this.afterTest = afterTest;
      this.beforeClass = beforeClass;
      this.afterClass = afterClass;
      this.beforeGroups = beforeGroups;
      this.afterGroups = afterGroups;
      this.beforeMethod = beforeMethod;
      this.afterMethod = afterMethod;
      this.templates = templates;
    }

    static FactoryConfigurationPrototypes build() {
      ITestMethodFinder finder = newFinder();
      Class<?> realClass = FactoryConfigurationSample.class;
      IObject.IdentifiableObject prototypeInstance =
          new IObject.IdentifiableObject(new FactoryConfigurationSample());

      ConfigurationTemplates templates =
          new ConfigurationTemplates(
              finder.getBeforeSuiteMethods(realClass),
              finder.getAfterSuiteMethods(realClass),
              finder.getBeforeTestConfigurationMethods(realClass),
              finder.getAfterTestConfigurationMethods(realClass),
              finder.getBeforeClassMethods(realClass),
              finder.getAfterClassMethods(realClass),
              finder.getBeforeGroupsConfigurationMethods(realClass),
              finder.getAfterGroupsConfigurationMethods(realClass),
              finder.getBeforeTestMethods(realClass),
              finder.getAfterTestMethods(realClass));

      return new FactoryConfigurationPrototypes(
          createSuite(templates.beforeSuite, true, prototypeInstance),
          createSuite(templates.afterSuite, false, prototypeInstance),
          createTest(templates.beforeTest, true, prototypeInstance),
          createTest(templates.afterTest, false, prototypeInstance),
          createClass(templates.beforeClass, true, prototypeInstance),
          createClass(templates.afterClass, false, prototypeInstance),
          createBeforeGroups(templates.beforeGroups, prototypeInstance),
          createAfterGroups(templates.afterGroups, prototypeInstance),
          createTestMethod(templates.beforeMethod, true, prototypeInstance),
          createTestMethod(templates.afterMethod, false, prototypeInstance),
          templates);
    }
  }

  private static ITestMethodFinder newFinder() {
    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setXmlClasses(XML_TEST.getXmlClasses());
    RunInfo runInfo = new RunInfo(() -> XML_TEST);
    runInfo.addMethodSelector(selector, 10);
    return new TestNGMethodFinder(OBJECT_FACTORY, runInfo, ANNOTATION_FINDER);
  }

  /** One method of every configuration type, matching the GITHUB-3436 reproducer shape. */
  public static class FactoryConfigurationSample {

    @BeforeSuite
    public void beforeSuite() {}

    @AfterSuite
    public void afterSuite() {}

    @BeforeTest
    public void beforeTest() {}

    @AfterTest
    public void afterTest() {}

    @BeforeClass
    public void beforeClass() {}

    @AfterClass
    public void afterClass() {}

    @BeforeMethod
    public void beforeMethod() {}

    @AfterMethod
    public void afterMethod() {}

    @BeforeGroups(groups = "a-group")
    public void beforeGroups() {}

    @AfterGroups(groups = "a-group")
    public void afterGroups() {}

    @Test(groups = "a-group")
    public void aTest() {}
  }

  private ConfigurationMethodBindMeasurement() {}
}
