package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * {@link ConfigurationMethod#bind(IObject.IdentifiableObject)} copies initialized metadata from a
 * prototype; priority must survive that copy so bound methods still sort the way the annotation
 * declared.
 */
public class ConfigurationMethodBindTest {

  @Test
  public void boundConfigurationMethodsKeepTheirPriority() {
    ITestNGMethod prototype = beforeMethodPrototype();
    assertThat(prototype.getPriority()).isEqualTo(7);

    ConfigurationMethod bound =
        ((ConfigurationMethod) prototype)
            .bind(new IObject.IdentifiableObject(new PrioritySample()));

    assertThat(bound.getPriority()).isEqualTo(7);
    assertThat(bound.getInstance()).isNotSameAs(prototype.getInstance());
  }

  /**
   * When a @Factory yields instances of different runtime classes, configuration methods that
   * inherit class-level groups must re-run {@link ConfigurationMethod#init()} on bind — otherwise
   * every instance keeps the prototype's groups.
   */
  @Test
  public void boundConfigurationMethodsReinitInheritedGroupsForDifferingRuntimeClass() {
    ConfigurationMethod prototype =
        (ConfigurationMethod) beforeClassPrototype(new RuntimeGroupsSubA());
    ConfigurationMethod boundSubA =
        prototype.bind(new IObject.IdentifiableObject(new RuntimeGroupsSubA()));
    ConfigurationMethod boundSubB =
        prototype.bind(new IObject.IdentifiableObject(new RuntimeGroupsSubB()));

    assertThat(boundSubA.getGroups()).containsExactly("group-a");
    assertThat(boundSubB.getGroups()).containsExactly("group-b");
  }

  /**
   * {@link ConfigurationMethod#clone()} keeps the historical copy set; bind carries more metadata.
   */
  @Test
  public void clonePreservesHistoricalMetadataButBindCopiesPriority() {
    ConfigurationMethod source = (ConfigurationMethod) beforeMethodPrototype();
    assertThat(source.getPriority()).isEqualTo(7);

    ConfigurationMethod cloned = source.clone();
    assertThat(cloned.getPriority()).isZero();

    ConfigurationMethod bound = source.bind(new IObject.IdentifiableObject(new PrioritySample()));
    assertThat(bound.getPriority()).isEqualTo(7);
  }

  private ITestNGMethod beforeMethodPrototype() {
    return testMethodConfigurationPrototype(PrioritySample.class, new PrioritySample());
  }

  private ITestNGMethod beforeClassPrototype(Object instance) {
    XmlSuite suite = new XmlSuite();
    XmlTest xmlTest = new XmlTest(suite);
    XmlClass xmlClass = new XmlClass(RuntimeGroupsBase.class.getName());
    xmlTest.getXmlClasses().add(xmlClass);

    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setXmlClasses(xmlTest.getXmlClasses());
    RunInfo runInfo = new RunInfo(() -> xmlTest);
    runInfo.addMethodSelector(selector, 10);

    ITestMethodFinder finder = new TestNGMethodFinder(objectFactory, runInfo, annotationFinder);
    ITestNGMethod[] templates = finder.getBeforeClassMethods(RuntimeGroupsBase.class);
    assertThat(templates).hasSize(1);

    IObject.IdentifiableObject prototypeInstance = new IObject.IdentifiableObject(instance);
    List<ITestNGMethod> prototypes =
        ConfigurationMethod.createClassConfigurationMethods(
            objectFactory, templates, annotationFinder, true, xmlTest, prototypeInstance);
    assertThat(prototypes).hasSize(1);
    return prototypes.get(0);
  }

  private ITestNGMethod testMethodConfigurationPrototype(Class<?> realClass, Object instance) {
    XmlSuite suite = new XmlSuite();
    XmlTest xmlTest = new XmlTest(suite);
    XmlClass xmlClass = new XmlClass(realClass.getName());
    xmlTest.getXmlClasses().add(xmlClass);

    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setXmlClasses(xmlTest.getXmlClasses());
    RunInfo runInfo = new RunInfo(() -> xmlTest);
    runInfo.addMethodSelector(selector, 10);

    ITestMethodFinder finder = new TestNGMethodFinder(objectFactory, runInfo, annotationFinder);
    ITestNGMethod[] templates = finder.getBeforeTestMethods(realClass);
    assertThat(templates).hasSize(1);

    IObject.IdentifiableObject prototypeInstance = new IObject.IdentifiableObject(instance);
    List<ITestNGMethod> prototypes =
        ConfigurationMethod.createTestMethodConfigurationMethods(
            objectFactory, templates, annotationFinder, true, xmlTest, prototypeInstance);
    assertThat(prototypes).hasSize(1);
    return prototypes.get(0);
  }

  public static class PrioritySample {

    @BeforeMethod(priority = 7)
    public void configure() {}

    @AfterMethod
    public void tearDown() {}

    @Test
    public void test() {}
  }

  public static class RuntimeGroupsBase {

    @BeforeClass
    public void beforeClass() {}

    @Test
    public void test() {}
  }

  @Test(groups = "group-a")
  public static class RuntimeGroupsSubA extends RuntimeGroupsBase {}

  @Test(groups = "group-b")
  public static class RuntimeGroupsSubB extends RuntimeGroupsBase {}

  private final DefaultTestObjectFactory objectFactory = new DefaultTestObjectFactory();
  private final IAnnotationFinder annotationFinder =
      new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
}
