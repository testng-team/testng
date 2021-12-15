package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.listeners.issue2638.DummyInvokedMethodListener;
import test.listeners.issue2638.TestClassASample;
import test.listeners.issue2638.TestClassBSample;
import test.listeners.issue2685.InterruptedTestSample;
import test.listeners.issue2685.SampleTestFailureListener;

public class ListenersTest extends SimpleBaseTest {

  private static final String[] github2638ExpectedList =
      new String[] {
        "test.listeners.issue2638.TestClassASample.testMethod",
        "test.listeners.issue2638.TestClassBSample.testMethod"
      };

  @Test(description = "GITHUB-2638", dataProvider = "suiteProvider")
  public void ensureDuplicateListenersAreNotWiredInAcrossSuites(
      XmlSuite xmlSuite, Map<String, String[]> expected) {
    TestNG testng = create(xmlSuite);
    testng.run();

    assertThat(DummyInvokedMethodListener.getMethods("Container_Suite"))
        .containsExactly(expected.get("Container_Suite"));
    assertThat(DummyInvokedMethodListener.getMethods("Inner_Suite1"))
        .containsExactly(expected.get("Inner_Suite1"));
    assertThat(DummyInvokedMethodListener.getMethods("Inner_Suite2"))
        .containsExactly(expected.get("Inner_Suite2"));
    DummyInvokedMethodListener.reset();
  }

  @Test(description = "GITHUB-2685")
  public void testThreadIsNotInterruptedInListener() {
    XmlSuite xmlSuite = createXmlSuite("2685_suite");
    xmlSuite.setParallel(XmlSuite.ParallelMode.CLASSES);
    createXmlTest(xmlSuite, "2685_test", InterruptedTestSample.class);
    TestNG testng = create(xmlSuite);
    SampleTestFailureListener listener = new SampleTestFailureListener();
    testng.addListener(listener);
    testng.run();

    Assertions.assertThat(listener.getInterruptedMethods()).isEmpty();
  }

  @DataProvider(name = "suiteProvider")
  public Object[][] getSuites() throws IOException {
    return new Object[][] {
      new Object[] {getNestedSuitesViaXmlFiles(), getExpectations()},
      new Object[] {getNestedSuitesViaApis(), getExpectations()},
      new Object[] {getNestedSuitesViaXmlFilesWithListenerInChildSuite(), getExpectations()},
      new Object[] {getNestedSuitesViaApisWithListenerInChildSuite(), getExpectations()}
    };
  }

  private static Map<String, String[]> getExpectations() {
    Map<String, String[]> expected = new HashMap<>();
    expected.put("Container_Suite", new String[] {});
    expected.put("Inner_Suite1", github2638ExpectedList);
    expected.put("Inner_Suite2", github2638ExpectedList);
    return expected;
  }

  private static XmlSuite getNestedSuitesViaXmlFiles() throws IOException {
    XmlSuite containerSuite = createXmlSuite("Container_Suite");
    containerSuite.setListeners(
        Collections.singletonList(DummyInvokedMethodListener.class.getName()));

    XmlSuite innerSuite1 = createXmlSuite("Inner_Suite1");
    createXmlTest(innerSuite1, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite1, "Inner_Test_2", TestClassBSample.class);
    Path s1 = Files.createTempFile("testng", ".xml");
    Files.write(s1, Collections.singletonList(innerSuite1.toXml()));

    XmlSuite innerSuite2 = createXmlSuite("Inner_Suite2");
    createXmlTest(innerSuite2, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite2, "Inner_Test_2", TestClassBSample.class);
    Path s2 = Files.createTempFile("testng", ".xml");
    Files.write(s2, Collections.singletonList(innerSuite2.toXml()));

    containerSuite.setSuiteFiles(
        Arrays.asList(s1.toFile().getAbsolutePath(), s2.toFile().getAbsolutePath()));
    return containerSuite;
  }

  private static XmlSuite getNestedSuitesViaXmlFilesWithListenerInChildSuite() throws IOException {
    XmlSuite containerSuite = createXmlSuite("Container_Suite");

    XmlSuite innerSuite1 = createXmlSuite("Inner_Suite1");
    innerSuite1.setListeners(Collections.singletonList(DummyInvokedMethodListener.class.getName()));
    createXmlTest(innerSuite1, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite1, "Inner_Test_2", TestClassBSample.class);
    Path s1 = Files.createTempFile("testng", ".xml");
    Files.write(s1, Collections.singletonList(innerSuite1.toXml()));

    XmlSuite innerSuite2 = createXmlSuite("Inner_Suite2");
    createXmlTest(innerSuite2, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite2, "Inner_Test_2", TestClassBSample.class);
    Path s2 = Files.createTempFile("testng", ".xml");
    Files.write(s2, Collections.singletonList(innerSuite2.toXml()));

    containerSuite.setSuiteFiles(
        Arrays.asList(s1.toFile().getAbsolutePath(), s2.toFile().getAbsolutePath()));
    return containerSuite;
  }

  private static XmlSuite getNestedSuitesViaApisWithListenerInChildSuite() {
    XmlSuite containerSuite = createXmlSuite("Container_Suite");

    XmlSuite innerSuite1 = createXmlSuite("Inner_Suite1");
    innerSuite1.setListeners(Collections.singletonList(DummyInvokedMethodListener.class.getName()));
    createXmlTest(innerSuite1, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite1, "Inner_Test_2", TestClassBSample.class);
    containerSuite.getChildSuites().add(innerSuite1);
    innerSuite1.setParentSuite(containerSuite);

    XmlSuite innerSuite2 = createXmlSuite("Inner_Suite2");
    createXmlTest(innerSuite2, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite2, "Inner_Test_2", TestClassBSample.class);
    containerSuite.getChildSuites().add(innerSuite2);
    innerSuite2.setParentSuite(containerSuite);
    return containerSuite;
  }

  private static XmlSuite getNestedSuitesViaApis() {
    XmlSuite containerSuite = createXmlSuite("Container_Suite");
    containerSuite.setListeners(
        Collections.singletonList(DummyInvokedMethodListener.class.getName()));
    XmlSuite innerSuite1 = createXmlSuite("Inner_Suite1");
    createXmlTest(innerSuite1, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite1, "Inner_Test_2", TestClassBSample.class);
    XmlSuite innerSuite2 = createXmlSuite("Inner_Suite2");
    createXmlTest(innerSuite2, "Inner_Test_1", TestClassASample.class);
    createXmlTest(innerSuite2, "Inner_Test_2", TestClassBSample.class);
    containerSuite.getChildSuites().add(innerSuite1);
    containerSuite.getChildSuites().add(innerSuite2);
    innerSuite1.setParentSuite(containerSuite);
    innerSuite2.setParentSuite(containerSuite);
    return containerSuite;
  }
}
