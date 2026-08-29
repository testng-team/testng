package org.testng;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
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
import org.testng.internal.IObject;
import org.testng.internal.RunInfo;
import org.testng.internal.TestNGMethodFinder;
import org.testng.internal.XmlMethodSelector;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * TestClass looks its configuration methods up once for the test class, not once per @Factory
 * instance. Each lookup rescans the whole class hierarchy, so a factory producing thousands of
 * instances used to pay for all ten of them that many times.
 *
 * <p>The lookup counts asserted here stand in for that cost, which is not otherwise observable —
 * the methods TestClass ends up with are the same either way, as the other tests in this class
 * check. They pin where the work is avoided, in the caller; a future change that made the lookups
 * themselves free, by memoizing inside the finder, would solve the same problem differently and
 * would be entitled to relax them.
 */
public class TestClassConfigurationLookupTest {

  private static final int INSTANCES = 3;

  @Test
  public void eachConfigurationCategoryIsLookedUpOncePerTestClass() {
    ITestMethodFinder finder = newFinder();

    newTestClass(finder, new FakeClass(INSTANCES));

    // verify() defaults to exactly once, and verifyNoMoreInteractions() closes the set, so this
    // says "these eleven lookups, once each, and nothing else" whatever the instance count.
    verify(finder).getTestMethods(Sample.class, xmlTest);
    verify(finder).getBeforeSuiteMethods(Sample.class);
    verify(finder).getAfterSuiteMethods(Sample.class);
    verify(finder).getBeforeTestConfigurationMethods(Sample.class);
    verify(finder).getAfterTestConfigurationMethods(Sample.class);
    verify(finder).getBeforeClassMethods(Sample.class);
    verify(finder).getAfterClassMethods(Sample.class);
    verify(finder).getBeforeGroupsConfigurationMethods(Sample.class);
    verify(finder).getAfterGroupsConfigurationMethods(Sample.class);
    verify(finder).getBeforeTestMethods(Sample.class);
    verify(finder).getAfterTestMethods(Sample.class);
    verifyNoMoreInteractions(finder);
  }

  @Test
  public void aClassThatProducedNoInstanceIsNotScannedForConfigurations() {
    ITestMethodFinder finder = newFinder();

    newTestClass(finder, new FakeClass(0));

    verify(finder).getTestMethods(Sample.class, xmlTest);
    verifyNoMoreInteractions(finder);
  }

  @Test
  public void theAccumulatedCategoriesAreBoundToEveryInstance() {
    FakeClass fakeClass = new FakeClass(INSTANCES);

    TestClass testClass = newTestClass(fakeClass);

    // @BeforeMethod/@AfterMethod and the test methods are accumulated across instances, so each
    // instance gets its own bound copy.
    assertThat(testClass.getBeforeTestMethods())
        .extracting(ITestNGMethod::getInstance)
        .containsExactlyElementsOf(instancesOf(fakeClass));
    assertThat(testClass.getAfterTestMethods())
        .extracting(ITestNGMethod::getInstance)
        .containsExactlyElementsOf(instancesOf(fakeClass));
    assertThat(testClass.getTestMethods())
        .extracting(ITestNGMethod::getInstance)
        .containsExactlyElementsOf(instancesOf(fakeClass));

    assertThat(testClass.getBeforeTestMethods())
        .extracting(ITestNGMethod::getMethodName)
        .containsOnly("beforeMethod");
    assertThat(testClass.getAfterTestMethods())
        .extracting(ITestNGMethod::getMethodName)
        .containsOnly("afterMethod");
    assertThat(testClass.getTestMethods())
        .extracting(ITestNGMethod::getMethodName)
        .containsOnly("aTest");
  }

  @Test
  public void classLevelConfigurationsAreIndexedByInstanceId() {
    FakeClass fakeClass = new FakeClass(INSTANCES);

    TestClass testClass = newTestClass(fakeClass);

    assertThat(testClass.getAllBeforeClassMethods()).hasSize(INSTANCES);
    assertThat(testClass.getAllAfterClassMethods()).hasSize(INSTANCES);
    for (IObject.IdentifiableObject each : fakeClass.objects) {
      assertThat(testClass.getInstanceBeforeClassMethods(each.getInstanceId()))
          .extracting(ITestNGMethod::getInstance)
          .containsExactly(each.getInstance());
      assertThat(testClass.getInstanceAfterClassMethods(each.getInstanceId()))
          .extracting(ITestNGMethod::getInstance)
          .containsExactly(each.getInstance());
    }
  }

  /**
   * The categories TestClass assigns rather than accumulates keep only the last instance's methods.
   * That is long-standing behaviour and the lookup hoisting does not change it; this pins it so a
   * later change to the binding cannot alter it unnoticed. @BeforeClass and @AfterClass are among
   * them: the per-instance copies live in the getInstanceBefore/AfterClassMethods maps, not in
   * these array accessors.
   */
  @Test
  public void theAssignedCategoriesStillKeepOnlyTheLastInstance() {
    FakeClass fakeClass = new FakeClass(INSTANCES);
    Object lastInstance = fakeClass.objects.get(INSTANCES - 1).getInstance();

    TestClass testClass = newTestClass(fakeClass);

    assertThat(testClass.getBeforeSuiteMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "beforeSuite"));
    assertThat(testClass.getAfterSuiteMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "afterSuite"));
    assertThat(testClass.getBeforeTestConfigurationMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "beforeTest"));
    assertThat(testClass.getAfterTestConfigurationMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "afterTest"));
    assertThat(testClass.getBeforeGroupsMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "beforeGroups"));
    assertThat(testClass.getAfterGroupsMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "afterGroups"));
    assertThat(testClass.getBeforeClassMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "beforeClass"));
    assertThat(testClass.getAfterClassMethods())
        .extracting(ITestNGMethod::getInstance, ITestNGMethod::getMethodName)
        .containsExactly(tuple(lastInstance, "afterClass"));
  }

  private static List<Object> instancesOf(FakeClass fakeClass) {
    return fakeClass.objects.stream()
        .map(IObject.IdentifiableObject::getInstance)
        .collect(Collectors.toList());
  }

  private final ITestObjectFactory objectFactory = new DefaultTestObjectFactory();
  private final IAnnotationFinder annotationFinder =
      new JDK15AnnotationFinder(new DefaultAnnotationTransformer());
  private final XmlSuite xmlSuite = new XmlSuite();
  private final XmlTest xmlTest = new XmlTest(xmlSuite);
  private final XmlClass xmlClass = new XmlClass(Sample.class.getName());

  {
    xmlTest.getXmlClasses().add(xmlClass);
  }

  /** The real finder, behind a mock that records what TestClass asked it for. */
  private ITestMethodFinder newFinder() {
    return mock(
        ITestMethodFinder.class,
        delegatesTo(new TestNGMethodFinder(objectFactory, newRunInfo(), annotationFinder)));
  }

  /**
   * The finder only answers methods a selector included, which is what TestRunner.initRunInfo sets
   * up; without it every category comes back empty.
   */
  private RunInfo newRunInfo() {
    XmlMethodSelector selector = new XmlMethodSelector();
    selector.setXmlClasses(xmlTest.getXmlClasses());
    RunInfo runInfo = new RunInfo(() -> xmlTest);
    runInfo.addMethodSelector(selector, 10);
    return runInfo;
  }

  private TestClass newTestClass(FakeClass fakeClass) {
    return newTestClass(newFinder(), fakeClass);
  }

  private TestClass newTestClass(ITestMethodFinder finder, FakeClass fakeClass) {
    return new TestClass(
        objectFactory, fakeClass, finder, annotationFinder, xmlTest, xmlClass, null);
  }

  /**
   * Stands in for what a @Factory leaves behind: one IClass holding several instances. Only
   * getObjects and the metadata getters are reached while a TestClass is being built; the rest are
   * there to satisfy the interface. A fake, so the {@code @SuppressWarnings} below are the members
   * {@link IClass} still declares and deprecates rather than uses of a deprecated API.
   */
  private static class FakeClass implements IClass, IObject {

    private final List<IObject.IdentifiableObject> objects = new ArrayList<>();

    FakeClass(int instances) {
      for (int i = 0; i < instances; i++) {
        objects.add(new IObject.IdentifiableObject(new Sample()));
      }
    }

    @Override
    public IdentifiableObject[] getObjects(boolean create, @Nullable String errorMsgPrefix) {
      return objects.toArray(new IdentifiableObject[0]);
    }

    @Override
    public void addObject(IdentifiableObject instance) {
      objects.add(instance);
    }

    @Override
    public String getName() {
      return Sample.class.getName();
    }

    @Override
    public @Nullable XmlTest getXmlTest() {
      return null;
    }

    @Override
    public @Nullable XmlClass getXmlClass() {
      return null;
    }

    @Override
    public @Nullable String getTestName() {
      return null;
    }

    @Override
    public Class<?> getRealClass() {
      return Sample.class;
    }

    @Override
    public long[] getObjectHashCodes() {
      throw new UnsupportedOperationException("not reached while building a TestClass");
    }

    @SuppressWarnings("deprecation")
    @Override
    public long[] getInstanceHashCodes() {
      return getObjectHashCodes();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object[] getInstances(boolean create) {
      throw new UnsupportedOperationException("not reached while building a TestClass");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addInstance(Object instance) {
      throw new UnsupportedOperationException("not reached while building a TestClass");
    }
  }

  /** Carries one method of every configuration type. */
  public static class Sample {

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
}
