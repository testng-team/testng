package test.annotationtransformer;

import org.assertj.core.api.iterable.Extractor;
import org.testng.Assert;
import org.testng.IAnnotationTransformer;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import test.SimpleBaseTest;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationTransformerTest extends SimpleBaseTest {

  private static final Extractor NAME_EXTRACTOR = new Extractor<ITestResult, String>() {
    @Override
    public String extract(ITestResult input) {
      return input.getName();
    }
  };

  /**
   * Make sure that without a transformer in place, a class-level
   * annotation invocationCount is correctly used.
   */
  @Test
  public void verifyAnnotationWithoutTransformer() {
    TestNG tng = create(AnnotationTransformerSampleTest.class);
    tng.setPreserveOrder(true);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    assertThat(tla.getPassedTests()).extracting(NAME_EXTRACTOR)
        .containsExactly(
            "five",
            "four", "four", "four", "four", "four",
            "three", "three", "three", "three", "three",
            "two", "two"
        );
    assertThat(tla.getFailedTests()).extracting(NAME_EXTRACTOR)
        .containsExactly("verify");
  }

  /**
   * Test a transformer on a method-level @Test
   */
  @Test
  public void verifyAnnotationTransformerMethod() {
    TestNG tng = create(AnnotationTransformerSampleTest.class);
    tng.setPreserveOrder(true);

    MyTransformer transformer = new MyTransformer();
    tng.setAnnotationTransformer(transformer);

    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    assertThat(transformer.getMethodNames()).contains("two", "three", "four", "five", "verify");

    assertThat(tla.getPassedTests()).extracting(NAME_EXTRACTOR)
        .containsExactly(
            "five", "five", "five", "five", "five",
            "four", "four", "four", "four",
            "three", "three", "three",
            "two", "two",
            "verify"
        );
    assertThat(tla.getFailedTests()).isEmpty();
  }

  @Test
  public void verifyAnnotationTransformerHasOnlyOneNonNullArgument() {
    TestNG tng = create(AnnotationTransformerSampleTest.class);

    MyParamTransformer transformer = new MyParamTransformer();
    tng.setAnnotationTransformer(transformer);

    tng.run();

    assertThat(transformer.isSuccess()).isTrue();
  }

  @Test
  public void verifyMyParamTransformerOnlyOneNonNull() {
    assertThat(MyParamTransformer.onlyOneNonNull(null, null, null)).isFalse();
    assertThat(MyParamTransformer.onlyOneNonNull(
        MyParamTransformer.class, MyParamTransformer.class.getConstructors()[0], null)).isFalse();
    assertThat(MyParamTransformer.onlyOneNonNull(MyParamTransformer.class, null, null)).isTrue();
  }

  /**
   * Without an annotation transformer, we should have zero
   * passed tests and one failed test called "one".
   */
  @Test
  public void verifyAnnotationTransformerClass2() {
    runTest(null, null, "one");
  }

  /**
   * With an annotation transformer, we should have one passed
   * test called "one" and zero failed tests.
   */
  @Test
  public void verifyAnnotationTransformerClass() {
    runTest(new MyTimeOutTransformer(), "one", null);
  }

  private void runTest(IAnnotationTransformer transformer,
      String passedName, String failedName)
  {
    MySuiteListener.triggered = false;
    MySuiteListener2.triggered = false;
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    if (transformer != null) {
      tng.setAnnotationTransformer(transformer);
    }
    tng.setTestClasses(new Class[] { AnnotationTransformerClassSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    List<ITestResult> results =
      passedName != null ? tla.getPassedTests() : tla.getFailedTests();
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
    tng.setVerbose(0);
    tng.setAnnotationTransformer(new MyListenerTransformer());
    tng.setTestClasses(new Class[]{AnnotationTransformerClassSampleTest.class});

    tng.run();
    Assert.assertFalse(MySuiteListener.triggered);
    Assert.assertTrue(MySuiteListener2.triggered);
  }

  @Test
  public void verifyConfigurationTransformer() {
    TestNG tng = new TestNG();
    tng.setAnnotationTransformer(new ConfigurationTransformer());
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { ConfigurationSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(ConfigurationSampleTest.getBefore(), "correct");
  }

  @Test
  public void verifyDataProviderTransformer() {
    TestNG tng = create();
    tng.setAnnotationTransformer(new DataProviderTransformer());
    tng.setTestClasses(new Class[] { AnnotationTransformerDataProviderSampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }

  @Test
  public void verifyFactoryTransformer() {
    TestNG tng = create();
    tng.setAnnotationTransformer(new FactoryTransformer());
    tng.setTestClasses(new Class[] { AnnotationTransformerFactorySampleTest.class});
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);
  }

  @Test(description = "Test for issue #605")
  public void verifyInvocationCountTransformer() {
    TestNG tng = create();
    tng.setTestClasses(new Class[] { AnnotationTransformerInvocationCountTest.class });
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 3);

    tng = create();
    tng.setAnnotationTransformer(new AnnotationTransformerInvocationCountTest.InvocationCountTransformer(5));
    tng.setTestClasses(new Class[]{AnnotationTransformerInvocationCountTest.class});
    tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 5);
  }

  @Test
  public void annotationTransformerInXmlShouldBeRun() throws Exception {
    String xml = "<suite name=\"SingleSuite\" >" +
        "  <listeners>" +
        "    <listener class-name=\"test.annotationtransformer.AnnotationTransformerInTestngXml\" />" +
        "  </listeners>" +
        "  <test enabled=\"true\" name=\"SingleTest\">" +
        "    <classes>" +
        "      <class name=\"test.annotationtransformer.AnnotationTransformerInTestngXml\" />" +
        "    </classes>" +
        "  </test>" +
        "</suite>"
        ;

    ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
    Collection<XmlSuite> suites = new Parser(is).parse();

    TestNG tng = create();
    tng.setXmlSuites(Arrays.asList(suites.toArray(new XmlSuite[0])));
    TestListenerAdapter tla = new TestListenerAdapter();
    tng.addListener(tla);

    tng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 1);

  }
}
