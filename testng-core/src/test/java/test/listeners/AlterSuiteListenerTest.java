package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.testng.IAlterSuiteListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class AlterSuiteListenerTest extends SimpleBaseTest {

  public static final String ALTER_SUITE_LISTENER = "AlterSuiteListener";

  @Test
  public void executionListenerWithXml() {
    XmlSuite suite =
        runTest(AlterSuiteListener1SampleTest.class, AlterSuiteNameListener.class.getName())
            .second();
    assertThat(suite.getName()).isEqualTo(AlterSuiteNameListener.class.getSimpleName());
  }

  @Test
  public void executionListenerWithoutListener() {
    XmlSuite suite = runTest(AlterSuiteListener1SampleTest.class).second();
    assertThat(suite.getName()).isEqualTo(ALTER_SUITE_LISTENER);
  }

  @Test
  public void executionListenerWithXml2() {
    XmlSuite suite =
        runTest(AlterSuiteListener1SampleTest.class, AlterXmlTestsInSuiteListener.class.getName())
            .second();
    assertThat(suite.getTests()).hasSize(2);
  }

  @Test(description = "GITHUB-2469")
  public void executionListenerWithXml3() {
    Pair<TestNG, XmlSuite> retObjects =
        runTest(
            AlterSuiteListener1SampleTest.class,
            AlterXmlTestWithParameterInSuiteListener.class.getName(),
            AlteredXmlSuiteReadListener.class.getName());
    TestNG tng = retObjects.first();
    XmlSuite suite = retObjects.second();
    assertThat(suite.getTests()).hasSize(2);
    List<ISuiteListener> listeners =
        Optional.ofNullable(tng.getSuiteListeners()).orElse(new ArrayList<>());
    assertThat(listeners).isNotEmpty();
    for (ISuiteListener iSuiteListener : listeners) {
      if (iSuiteListener instanceof AlteredXmlSuiteReadListener) {
        AlteredXmlSuiteReadListener alteredXmlSuiteReadListener =
            (AlteredXmlSuiteReadListener) iSuiteListener;
        XmlSuite xmlSuite = alteredXmlSuiteReadListener.currentSuiteOnStart.getXmlSuite();
        List<XmlTest> tests = xmlSuite.getTests();
        int i = 1;
        for (XmlTest xmlTest : tests) {
          assertThat(xmlTest.getParameter("param")).isEqualTo(String.valueOf(i));
          i++;
        }
      }
    }
  }

  private Pair<TestNG, XmlSuite> runTest(Class<?> listenerClass, String... listenerNames) {
    XmlSuite s = createXmlSuite(ALTER_SUITE_LISTENER);
    createXmlTest(s, "Test", listenerClass.getName());

    for (String listenerName : listenerNames) {
      s.addListener(listenerName);
    }
    TestNG tng = create();
    tng.setXmlSuites(List.of(s));
    tng.run();
    return new Pair<>(tng, s);
  }

  public static class AlterSuiteListener1SampleTest {
    @Test
    public void foo() {}
  }

  public static class AlterSuiteNameListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
      XmlSuite suite = suites.get(0);
      suite.setName(getClass().getSimpleName());
    }
  }

  public static class AlterXmlTestsInSuiteListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
      XmlSuite suite = suites.get(0);
      List<XmlTest> tests = suite.getTests();
      XmlTest test = tests.get(0);
      XmlTest anotherTest = new XmlTest(suite);
      anotherTest.setName("foo");
      anotherTest.setClasses(test.getClasses());
    }
  }

  public static class AlterXmlTestWithParameterInSuiteListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
      XmlSuite suite = suites.get(0);
      List<XmlTest> tests = suite.getTests();
      XmlTest test = tests.get(0);

      List<XmlTest> newXmlTests = new ArrayList<>();
      XmlTest newXmlTest = (XmlTest) test.clone();
      newXmlTest.setName("name_1");
      newXmlTest.addParameter("param", "1");
      newXmlTests.add(newXmlTest);

      newXmlTest = (XmlTest) test.clone();
      newXmlTest.setName("name_2");
      newXmlTest.addParameter("param", "2");
      newXmlTests.add(newXmlTest);

      suite.setTests(newXmlTests);
    }
  }

  public static class AlteredXmlSuiteReadListener implements ISuiteListener {

    public ISuite currentSuiteOnStart;

    @Override
    public void onStart(ISuite suite) {
      currentSuiteOnStart = suite;
    }
  }
}
