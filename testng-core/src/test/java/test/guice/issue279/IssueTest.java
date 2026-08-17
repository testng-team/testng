package test.guice.issue279;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.testng.CommandLineArgs;
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
    MyListenerWithoutModuleFactory.clearInstance();
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

  @Test(description = "GITHUB-3377")
  public void setListenerClassesDoesNotThrowWhenListenerIsGuiceAnnotated() {
    TestNG testng = new TestNG();
    testng.setListenerClasses(List.of(MyListener.class));
  }

  @Test(description = "GITHUB-3377")
  public void setListenerClassesInjectsGuiceAnnotatedListeners() {
    TestNG testng = create(TestClassWithoutListener.class);
    testng.setListenerClasses(List.of(MyListener.class, DummyReporter.class));
    testng.run();
    assertThat(MyListener.getInstance()).isInstanceOf(TextGreeter.class);
    assertThat(DummyReporter.getInstance()).isInstanceOf(TextGreeter.class);
  }

  @Test(description = "GITHUB-3377")
  public void cliListenerDoesNotThrowWhenListenerIsGuiceAnnotated() {
    CommandLineArgs args = new CommandLineArgs();
    args.listener = MyListener.class.getName() + "," + DummyReporter.class.getName();
    args.testClass = TestClassWithoutListener.class.getName();
    args.useDefaultListeners = "false";
    ConfigurableTestNG testng = new ConfigurableTestNG();
    testng.configure(args);
    testng.run();
    assertThat(MyListener.getInstance()).isInstanceOf(TextGreeter.class);
    assertThat(DummyReporter.getInstance()).isInstanceOf(TextGreeter.class);
  }

  @Test(description = "GITHUB-3377")
  public void cliListenerInheritsGuiceParentModuleFromSuite() throws IOException {
    XmlSuite xmlSuite = createXmlSuite("sample_suite");
    xmlSuite.setParentModule(SampleModule.class.getName());
    createXmlTest(xmlSuite, "sample_test", TestClassWithoutListener.class);
    Path suiteFile = Files.createTempFile("issue3377", ".xml");
    Files.write(suiteFile, xmlSuite.toXml().getBytes(StandardCharsets.UTF_8));

    CommandLineArgs args = new CommandLineArgs();
    args.listener =
        MyListenerWithoutModuleFactory.class.getName()
            + ","
            + DummyReporterWithoutModuleFactory.class.getName();
    args.suiteFiles = List.of(suiteFile.toString());
    args.useDefaultListeners = "false";
    ConfigurableTestNG testng = new ConfigurableTestNG();
    testng.configure(args);
    testng.run();
    assertThat(MyListenerWithoutModuleFactory.getInstance()).isInstanceOf(TextGreeter.class);
    assertThat(DummyReporterWithoutModuleFactory.getInstance()).isInstanceOf(Car.class);
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

  private static final class ConfigurableTestNG extends TestNG {
    @Override
    public void configure(CommandLineArgs cla) {
      super.configure(cla);
    }
  }
}
