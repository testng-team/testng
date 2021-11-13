package test.beforegroups;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.PackageUtils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlGroups;
import org.testng.xml.XmlRun;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.beforegroups.issue118.TestclassSample;
import test.beforegroups.issue1694.BaseClassWithBeforeGroups;
import test.beforegroups.issue2229.AnotherTestClassSample;
import test.beforegroups.issue2229.TestClassSample;
import test.beforegroups.issue346.SampleTestClass;

public class BeforeGroupsTest extends SimpleBaseTest {
  @Test
  public void testInSequentialMode() throws IOException {
    runTest(XmlSuite.ParallelMode.NONE);
  }

  @Test
  public void testParallelMode() throws IOException {
    runTest(XmlSuite.ParallelMode.CLASSES);
  }

  @Test(description = "GITHUB-118")
  public void ensureInheritedAttributeWorksForBeforeGroups() {
    XmlSuite xmlSuite = createXmlSuite("suite", "test", TestclassSample.class);
    xmlSuite.addIncludedGroup("group1");
    TestNG testng = create(xmlSuite);
    TestListenerAdapter listener = new TestListenerAdapter();
    testng.addListener(listener);
    testng.run();
    assertThat(listener.getFailedTests()).isEmpty();
  }

  @Test(description = "GITHUB-346")
  public void ensureBeforeGroupsAreInvokedWhenCoupledWithAfterGroups() {
    String TEST_1 = "A";
    String TEST_2 = "B";

    XmlSuite xmlSuite = new XmlSuite();
    xmlSuite.setName("346_suite");
    createXmlTest(xmlSuite, TEST_1, "A");
    createXmlTest(xmlSuite, TEST_2, "B");
    TestNG testng = new TestNG();
    testng.setXmlSuites(Collections.singletonList(xmlSuite));
    testng.run();
    Map<String, List<String>> expected = new HashMap<>();
    expected.put(TEST_1, Collections.singletonList("beforeGroups:" + TEST_1 + TEST_1));
    expected.put(TEST_2, Collections.singletonList("afterGroups:" + TEST_2 + TEST_2));
    assertThat(SampleTestClass.logs).isEqualTo(expected);
  }

  @Test(description = "GITHUB-2229")
  public void ensureBeforeGroupsAreInvokedByDefaultEvenWithoutGrouping() {
    TestNG testng = create(TestClassSample.class, AnotherTestClassSample.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(0);
    List<String> expectedLogs =
        Arrays.asList(
            "TestA",
            "TestB",
            "TestC",
            "beforeGroupA",
            "testGroupA1",
            "testGroupA2",
            "testGroupA3",
            "afterGroupA",
            "beforeGroupB",
            "testGroupB",
            "afterGroupB");
    assertThat(TestClassSample.logs).containsExactlyElementsOf(expectedLogs);
    expectedLogs =
        Arrays.asList(
            "beforeGroups1",
            "test1_testGroup1",
            "beforeGroups2",
            "test2_testGroup2",
            "test3_testGroup1",
            "afterGroups1",
            "test4_testGroup2",
            "afterGroups2");
    assertThat(AnotherTestClassSample.logs).containsExactlyElementsOf(expectedLogs);
  }

  private static void createXmlTest(XmlSuite xmlSuite, String name, String group) {
    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setName(name);
    xmlTest.setClasses(Collections.singletonList(new XmlClass(SampleTestClass.class)));
    xmlTest.setGroups(groups(group));
  }

  private static XmlGroups groups(String group) {
    XmlGroups xmlGroups = new XmlGroups();
    XmlRun xmlRun = new XmlRun();
    xmlRun.onInclude(group);
    xmlGroups.setRun(xmlRun);
    return xmlGroups;
  }

  private static void runTest(XmlSuite.ParallelMode mode) throws IOException {
    XmlSuite suite = createXmlSuite("sample_suite");
    String pkg = BaseClassWithBeforeGroups.class.getPackage().getName();
    Class<?>[] classes = findClassesInPackage(pkg);
    XmlTest xmlTest = createXmlTestWithPackages(suite, "sample_test", classes);
    xmlTest.addIncludedGroup("regression");
    xmlTest.setParallel(mode);
    TestNG tng = create(suite);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.run();
    List<String> beforeGroups = Lists.newArrayList();
    List<String> afterGroups = Lists.newArrayList();
    for (String name : listener.getInvokedMethodNames()) {
      if (name.equalsIgnoreCase(BaseClassWithBeforeGroups.BEFORE_GROUPS)) {
        beforeGroups.add(name);
      }
      if (name.equalsIgnoreCase(BaseClassWithBeforeGroups.AFTER_GROUPS)) {
        afterGroups.add(name);
      }
    }
    assertThat(beforeGroups).containsOnly(BaseClassWithBeforeGroups.BEFORE_GROUPS);
    assertThat(afterGroups).containsOnly(BaseClassWithBeforeGroups.AFTER_GROUPS);
  }

  private static Class<?>[] findClassesInPackage(String packageName) throws IOException {
    String[] classes =
        PackageUtils.findClassesInPackage(packageName, new ArrayList<>(), new ArrayList<>());
    List<Class<?>> loadedClasses = new ArrayList<>();
    for (String clazz : classes) {
      loadedClasses.add(ClassHelper.forName(clazz));
    }
    return loadedClasses.toArray(new Class<?>[0]);
  }
}
