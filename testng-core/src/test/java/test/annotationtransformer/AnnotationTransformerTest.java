package test.annotationtransformer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.iterable.Extractor;
import org.testng.Assert;
import org.testng.IAnnotationTransformer;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;
import test.SimpleBaseTest;
import test.TestHelper;
import test.annotationtransformer.issue1790.TestClassSample1;
import test.annotationtransformer.issue1790.TestClassSample2;
import test.annotationtransformer.issue1790.TransformerImpl;
import test.annotationtransformer.issue2312.RetryListener;
import test.annotationtransformer.issue2312.SampleTestClass;
import test.annotationtransformer.issue2536.data.nested.ClassContainer;
import test.annotationtransformer.issue2536.data.nested.ClassContainer.NonGroupClass3;
import test.annotationtransformer.issue2536.listeners.ExternalGroupManager;

public class AnnotationTransformerTest extends SimpleBaseTest {

  private static final Extractor<ITestResult, String> NAME_EXTRACTOR = ITestResult::getName;

  /**
   * Make sure that without a transformer in place, a class-level annotation invocationCount is
   * correctly used.
   */
  @Test
  public void verifyAnnotationWithoutTransformer() {
    TestNG tng = create(AnnotationTransformerSampleTest.class);
    tng.setPreserveOrder(true);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    assertThat(tla.getPassedTests())
        .extracting(NAME_EXTRACTOR)
        .containsExactly(
            "five", "four", "four", "four", "four", "four", "three", "three", "three", "three",
            "three", "two", "two");
    assertThat(tla.getFailedTests()).extracting(NAME_EXTRACTOR).containsExactly("verify");
  }

  /** Test a transformer on a method-level @Test */
  @Test
  public void verifyAnnotationTransformerMethod() {
    TestNG tng = create(AnnotationTransformerSampleTest.class);
    tng.setPreserveOrder(true);

    MyTransformer transformer = new MyTransformer();
    tng.addListener(transformer);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    assertThat(transformer.getMethodNames()).contains("two", "three", "four", "five", "verify");

    assertThat(tla.getPassedTests())
        .extracting(NAME_EXTRACTOR)
        .containsExactly(
            "five", "five", "five", "five", "five", "four", "four", "four", "four", "three",
            "three", "three", "two", "two", "verify");
    assertThat(tla.getFailedTests()).isEmpty();
  }

  @Test
  public void verifyAnnotationTransformerHasOnlyOneNonNullArgument() {
    TestNG tng = create(AnnotationTransformerSampleTest.class);

    MyParamTransformer transformer = new MyParamTransformer();
    tng.addListener(transformer);

    tng.run();

    assertThat(transformer.isSuccess()).isTrue();
  }

  @Test
  public void verifyMyParamTransformerOnlyOneNonNull() {
    assertThat(MyParamTransformer.onlyOneNonNull(null, null, null)).isFalse();
    assertThat(
            MyParamTransformer.onlyOneNonNull(
                MyParamTransformer.class, MyParamTransformer.class.getConstructors()[0], null))
        .isFalse();
    assertThat(MyParamTransformer.onlyOneNonNull(MyParamTransformer.class, null, null)).isTrue();
  }

  /**
   * Without an annotation transformer, we should have zero passed tests and one failed test called
   * "one".
   */
  @Test
  public void verifyAnnotationTransformerClass2() {
    runTest(null, null, "one");
  }

  /**
   * With an annotation transformer, we should have one passed test called "one" and zero failed
   * tests.
   */
  @Test
  public void verifyAnnotationTransformerClass() {
    runTest(new MyTimeOutTransformer(), "one", null);
  }

  private void runTest(IAnnotationTransformer transformer, String passedName, String failedName) {
    MySuiteListener.triggered = false;
    MySuiteListener2.triggered = false;
    TestNG tng = new TestNG();
    if (transformer != null) {
      tng.addListener(transformer);
    }
    tng.setTestClasses(new Class[] {AnnotationTransformerClassSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    List<ITestResult> results = passedName != null ? tla.getPassedTests() : tla.getFailedTests();
    String name = passedName != null ? passedName : failedName;

    Assert.assertEquals(results.size(), 1);
    Assert.assertEquals(name, results.get(0).getMethod().getMethodName());
    Assert.assertTrue(MySuiteListener.triggered);
    Assert.assertFalse(MySuiteListener2.triggered);
  }

  @Test
  public void verifyListenerAnnotationTransformerClass() {
    MySuiteListener.triggered = false;
    MySuiteListener2.triggered = false;
    TestNG tng = new TestNG();
    tng.addListener(new MyListenerTransformer());
    tng.setTestClasses(new Class[] {AnnotationTransformerClassSampleTest.class});

    tng.run();
    Assert.assertFalse(MySuiteListener.triggered);
    Assert.assertTrue(MySuiteListener2.triggered);
  }

  @Test
  public void verifyConfigurationTransformer() {
    TestNG tng = new TestNG();
    tng.addListener(new ConfigurationTransformer());
    tng.setTestClasses(new Class[] {ConfigurationSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(ConfigurationSampleTest.getBefore(), "correct");
  }

  @Test
  public void verifyDataProviderTransformer() {
    TestNG tng = create();
    tng.addListener(new DataProviderTransformer());
    tng.setTestClasses(new Class[] {AnnotationTransformerDataProviderSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }

  public static class CustomizedTestListenerAdaptor extends TestListenerAdapter {}

  @Test(description = "GITHUB-2138")
  public void verifyAnnotationTransformerInvocationForAllApplicableEvents() {
    TestNG tng = create();
    FactoryTransformer transformer = new FactoryTransformer();
    tng.addListener(transformer);
    tng.setTestClasses(new Class[] {AnnotationTransformerFactorySampleTest.class});
    CustomizedTestListenerAdaptor tla = new CustomizedTestListenerAdaptor();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
    String[] expectedLogs =
        new String[] {
          "transform_data_provider",
          "transform_factory",
          "transform_test",
          "transform_config",
          "transform_listener"
        };
    assertThat(transformer.getLogs()).containsExactly(expectedLogs);
  }

  @Test(description = "Test for issue #605")
  public void verifyInvocationCountTransformer() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] {AnnotationTransformerInvocationCountTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 3);

    tng = create();
    tng.addListener(new AnnotationTransformerInvocationCountTest.InvocationCountTransformer(5));
    tng.setTestClasses(new Class[] {AnnotationTransformerInvocationCountTest.class});
    tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 5);
  }

  @Test
  public void annotationTransformerInXmlShouldBeRun() throws Exception {
    String xml =
        TestHelper.SUITE_XML_HEADER
            + "<suite name=\"SingleSuite\" >"
            + "  <listeners>"
            + "    <listener class-name=\"test.annotationtransformer.AnnotationTransformerInTestngXml\" />"
            + "  </listeners>"
            + "  <test enabled=\"true\" name=\"SingleTest\">"
            + "    <classes>"
            + "      <class name=\"test.annotationtransformer.AnnotationTransformerInTestngXml\" />"
            + "    </classes>"
            + "  </test>"
            + "</suite>";

    ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
    Collection<XmlSuite> suites = new Parser(is).parse();

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(suites.toArray(new XmlSuite[0])));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }

  @Test(description = "GITHUB-1790")
  public void ensureAnnotationTransformerInvokedForClassLevelTestAnnotations() {
    TestNG testng = create(TestClassSample1.class, TestClassSample2.class);
    TransformerImpl transformer = new TransformerImpl();
    testng.addListener(transformer);
    testng.run();
    assertThat(transformer.getClasses()).hasSize(2);
    assertThat(transformer.getConstructors()).isEmpty();
    assertThat(transformer.getMethods()).hasSize(2);
  }

  @Test(description = "GITHUB-2312")
  public void ensureAnnotationTransformerIsNotInvokedMultipleTimes() {
    XmlTest myTest = createXmlTest("sample_suite", "sample_test", SampleTestClass.class);
    myTest.getSuite().addListener(RetryListener.class.getName());
    TestNG myTestNG = create(myTest.getSuite());
    myTestNG.run();
    int retried = RetryListener.getExecutionCount();
    assertThat(retried).isEqualTo(1);
  }

  @Test(description = "GITHUB-2536", dataProvider = "getXmlPackages")
  public void testNestedTestClass_packageIncludesNormalTestClasses(List<XmlPackage> xmlPackages) {
    ExternalGroupManager groupManager = new ExternalGroupManager();
    String group = "run-me-dynamically";
    String container = ClassContainer.class.getName();
    String inner = NonGroupClass3.class.getSimpleName();

    groupManager.addGroupDefinition(container + "$" + inner, group);

    TestListenerAdapter tla = new TestListenerAdapter();

    XmlSuite xmlSuite = createXmlSuite("2536_suite");
    XmlTest myTest = createXmlTest(xmlSuite, "2536_test");
    myTest.addIncludedGroup(group);
    TestNG testng = create(xmlSuite);
    testng.addListener(tla);
    testng.addListener(groupManager);

    myTest.setXmlPackages(xmlPackages);

    testng.run();
    assertThat(tla.getFailedTests()).withFailMessage("We should have no failed tests").isEmpty();

    assertThat(tla.getPassedTests()).withFailMessage("We should have 2 passed tests").hasSize(2);

    long count =
        tla.getPassedTests().stream()
            .filter(m -> m.getInstance().getClass().equals(NonGroupClass3.class))
            .count();

    assertThat(count)
        .withFailMessage("We should have tests that only belong to " + NonGroupClass3.class)
        .isEqualTo(2);
  }

  @DataProvider
  public Object[][] getXmlPackages() {
    return new Object[][] {
      {Collections.singletonList(new XmlPackage("test.annotationtransformer.issue2536.data.*"))},
      {
        Arrays.asList(
            new XmlPackage("test.annotationtransformer.issue2536.data.normal"),
            new XmlPackage("test.annotationtransformer.issue2536.data.nested"))
      }
    };
  }
}
