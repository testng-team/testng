package test.guice.issue279;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @BeforeMethod
  public void cleanup() {
    MyListener.clearInstance();
    DummyReporter.clearInstance();
    DummyReporterWithoutModuleFactory.clearInstance();
  }

  @Test
  public void classWithListenerAnnotation() {
    TestNG testng = create(TestClassWithListener.class);
    testng.run();
    assertThat(MyListener.getInstance()).isInstanceOf(TextGreeter.class);
    assertThat(DummyReporter.getInstance()).isInstanceOf(TextGreeter.class);
  }

  @Test
  public void classWithoutListenerAnnotation() {
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    xmlSuite.setListeners(Arrays.asList(MyListener.class.getName(), DummyReporter.class.getName()));
    createXmlTest(xmlSuite, "sample_test", TestClassWithoutListener.class);
    TestNG testng = create(xmlSuite);
    testng.run();
    assertThat(MyListener.getInstance()).isInstanceOf(TextGreeter.class);
    assertThat(DummyReporter.getInstance()).isInstanceOf(TextGreeter.class);
  }

  @Test
  public void classWithModuleDefinedInSuite() {
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    xmlSuite.setParentModule(SampleModule.class.getName());
    xmlSuite.setListeners(
        Arrays.asList(
            MyListenerWithoutModuleFactory.class.getName(),
            DummyReporterWithoutModuleFactory.class.getName()));
    createXmlTest(xmlSuite, "sample_test", TestClassWithoutListener.class);
    TestNG testng = create(xmlSuite);
    testng.run();
    assertThat(MyListenerWithoutModuleFactory.getInstance()).isInstanceOf(TextGreeter.class);
    assertThat(DummyReporterWithoutModuleFactory.getInstance()).isInstanceOf(Car.class);
  }
}
