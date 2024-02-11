package test.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.ExitCode;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.listeners.issue2638.DummyInvokedMethodListener;
import test.listeners.issue2638.TestClassASample;
import test.listeners.issue2638.TestClassBSample;
import test.listeners.issue2685.InterruptedTestSample;
import test.listeners.issue2685.SampleTestFailureListener;
import test.listeners.issue2752.ListenerSample;
import test.listeners.issue2752.TestClassSample;
import test.listeners.issue2771.TestCaseSample;
import test.listeners.issue2880.ListenerForIssue2880;
import test.listeners.issue2880.TestClassWithFailingConfigsSample;
import test.listeners.issue2880.TestClassWithPassingConfigsSample;
import test.listeners.issue2916.AlterSuiteListenerHolder;
import test.listeners.issue2916.AnnotatedTestCaseSamplesHolder;
import test.listeners.issue2916.AnnotationBackedListenerComparator;
import test.listeners.issue2916.ClassListenerHolder;
import test.listeners.issue2916.ConfigurationListenerHolder;
import test.listeners.issue2916.DataProviderInterceptorHolder;
import test.listeners.issue2916.DataProviderListenerHolder;
import test.listeners.issue2916.DataProviderSampleTestCase;
import test.listeners.issue2916.ElaborateSampleTestCase;
import test.listeners.issue2916.ExecutionListenerHolder;
import test.listeners.issue2916.ExecutionVisualiserHolder;
import test.listeners.issue2916.InvokedMethodListenerHolder;
import test.listeners.issue2916.MethodInterceptorHolder;
import test.listeners.issue2916.NormalSampleTestCase;
import test.listeners.issue2916.SimpleConfigTestCase;
import test.listeners.issue2916.SuiteListenerHolder;
import test.listeners.issue2916.TestListenerHolder;
import test.listeners.issue3064.EvidenceListener;
import test.listeners.issue3064.SampleTestCase;

public class ListenersTest extends SimpleBaseTest {
  public static final String[] github2638ExpectedList =
      new String[] {
        "test.listeners.issue2638.TestClassASample.testMethod",
        "test.listeners.issue2638.TestClassBSample.testMethod"
      };

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionListenersViaApi() {
    Ensure.orderingViaApi(
        ExecutionListenerHolder.LOGS,
        ExecutionListenerHolder.ALL,
        ExecutionListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForAlterSuiteListenersViaApi() {
    Ensure.orderingViaApi(
        AlterSuiteListenerHolder.LOGS,
        AlterSuiteListenerHolder.ALL,
        AlterSuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForSuiteListenersViaApi() {
    Ensure.orderingViaApi(
        SuiteListenerHolder.LOGS, SuiteListenerHolder.ALL, SuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForTestListenersViaApi() {
    Ensure.orderingViaApi(
        ElaborateSampleTestCase.class,
        TestListenerHolder.LOGS,
        TestListenerHolder.ALL,
        TestListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForInvokedListenersViaApi() {
    Ensure.orderingViaApi(
        InvokedMethodListenerHolder.LOGS,
        InvokedMethodListenerHolder.ALL,
        InvokedMethodListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForConfigurationListenersViaApi() {
    Ensure.orderingViaApi(
        SimpleConfigTestCase.class,
        ConfigurationListenerHolder.LOGS,
        ConfigurationListenerHolder.ALL,
        ConfigurationListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForClassListenersViaApi() {
    Ensure.orderingViaApi(
        ClassListenerHolder.LOGS, ClassListenerHolder.ALL, ClassListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderListenersViaApi() {
    Ensure.orderingViaApi(
        DataProviderSampleTestCase.class,
        DataProviderListenerHolder.LOGS,
        DataProviderListenerHolder.ALL,
        DataProviderListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderInterceptorsViaApi() {
    Ensure.orderingViaApi(
        DataProviderSampleTestCase.class,
        DataProviderInterceptorHolder.LOGS,
        DataProviderInterceptorHolder.ALL,
        DataProviderInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionVisualisersViaApi() {
    Ensure.orderingViaApi(
        ExecutionVisualiserHolder.LOGS,
        ExecutionVisualiserHolder.ALL,
        ExecutionVisualiserHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForMethodInterceptorsViaApi() {
    Ensure.orderingViaApi(
        MethodInterceptorHolder.LOGS,
        MethodInterceptorHolder.ALL,
        MethodInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        ExecutionListenerHolder.LOGS,
        ExecutionListenerHolder.ALL_STRING,
        ExecutionListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForAlterSuiteListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        AlterSuiteListenerHolder.LOGS,
        AlterSuiteListenerHolder.ALL_STRING,
        AlterSuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForSuiteListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        SuiteListenerHolder.LOGS,
        SuiteListenerHolder.ALL_STRING,
        SuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForTestListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        ElaborateSampleTestCase.class,
        TestListenerHolder.LOGS,
        TestListenerHolder.ALL_STRING,
        TestListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForInvokedMethodListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        InvokedMethodListenerHolder.LOGS,
        InvokedMethodListenerHolder.ALL_STRING,
        InvokedMethodListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForConfigurationListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        SimpleConfigTestCase.class,
        ConfigurationListenerHolder.LOGS,
        ConfigurationListenerHolder.ALL_STRING,
        ConfigurationListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForClassListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        ClassListenerHolder.LOGS,
        ClassListenerHolder.ALL_STRING,
        ClassListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderListenersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        DataProviderSampleTestCase.class,
        DataProviderListenerHolder.LOGS,
        DataProviderListenerHolder.ALL_STRING,
        DataProviderListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderInterceptorsViaXmlTag() {
    Ensure.orderingViaXmlTag(
        DataProviderSampleTestCase.class,
        DataProviderInterceptorHolder.LOGS,
        DataProviderInterceptorHolder.ALL_STRING,
        DataProviderInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionVisualisersViaXmlTag() {
    Ensure.orderingViaXmlTag(
        ExecutionVisualiserHolder.LOGS,
        ExecutionVisualiserHolder.ALL_STRING,
        ExecutionVisualiserHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForMethodInterceptorsViaXmlTag() {
    Ensure.orderingViaXmlTag(
        MethodInterceptorHolder.LOGS,
        MethodInterceptorHolder.ALL_STRING,
        MethodInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionListenersViaCli() {
    Ensure.orderingViaCli(
        ExecutionListenerHolder.LOGS,
        ExecutionListenerHolder.ALL_STRING,
        ExecutionListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForAlterSuiteListenersViaCli() {
    Ensure.orderingViaCli(
        AlterSuiteListenerHolder.LOGS,
        AlterSuiteListenerHolder.ALL_STRING,
        AlterSuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForSuiteListenersViaCli() {
    Ensure.orderingViaCli(
        SuiteListenerHolder.LOGS,
        SuiteListenerHolder.ALL_STRING,
        SuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForTestListenersViaCli() {
    Ensure.orderingViaCli(
        ElaborateSampleTestCase.class, TestListenerHolder.LOGS,
        TestListenerHolder.ALL_STRING, TestListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForInvokedMethodListenersViaCli() {
    Ensure.orderingViaCli(
        InvokedMethodListenerHolder.LOGS,
        InvokedMethodListenerHolder.ALL_STRING,
        InvokedMethodListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForConfigurationListenersViaCli() {
    Ensure.orderingViaCli(
        SimpleConfigTestCase.class,
        ConfigurationListenerHolder.LOGS,
        ConfigurationListenerHolder.ALL_STRING,
        ConfigurationListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForClassListenersViaCli() {
    Ensure.orderingViaCli(
        ClassListenerHolder.LOGS,
        ClassListenerHolder.ALL_STRING,
        ClassListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderListenersViaCli() {
    Ensure.orderingViaCli(
        DataProviderSampleTestCase.class,
        DataProviderListenerHolder.LOGS,
        DataProviderListenerHolder.ALL_STRING,
        DataProviderListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderInterceptorsViaCli() {
    Ensure.orderingViaCli(
        DataProviderSampleTestCase.class,
        DataProviderInterceptorHolder.LOGS,
        DataProviderInterceptorHolder.ALL_STRING,
        DataProviderInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionVisualisersViaCli() {
    Ensure.orderingViaCli(
        ExecutionVisualiserHolder.LOGS,
        ExecutionVisualiserHolder.ALL_STRING,
        ExecutionVisualiserHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForMethodInterceptorsViaCli() {
    Ensure.orderingViaCli(
        MethodInterceptorHolder.LOGS,
        MethodInterceptorHolder.ALL_STRING,
        MethodInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionListenersViaAnnotation() {
    // This is a special case scenario and so the expected value order is different from the
    // other two test cases from above.
    // This is because, when TestNG discovers an IExecutionListener implementation via
    // @Listeners annotation (which is the case with ExecutionListenerSampleTestCase) it would have
    // already invoked the first set of listeners found via <listeners> tag or service loaders or
    // via the TestNG API. So the IExecutionListener implementation is invoked as and when it's
    // discovered
    // That's why MasterOogway is listed at the 3rd position instead of it being at the first
    // position
    // But the onExecutionFinish() adheres to the proper order in symmetric order.
    String[] expected =
        new String[] {
          "MasterShifu.onExecutionStart",
          "DragonWarrior.onExecutionStart",
          "MasterOogway.onExecutionStart",
          "DragonWarrior.onExecutionFinish",
          "MasterShifu.onExecutionFinish",
          "MasterOogway.onExecutionFinish"
        };
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.ExecutionListenerSampleTestCase.class,
        ExecutionListenerHolder.LOGS,
        ExecutionListenerHolder.SUBSET,
        expected);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForSuiteListenersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.SuiteListenerSampleTestCase.class,
        SuiteListenerHolder.LOGS,
        SuiteListenerHolder.SUBSET,
        SuiteListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForTestListenersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.TestListenerSampleTestCase.class,
        TestListenerHolder.LOGS,
        TestListenerHolder.SUBSET,
        TestListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForInvokedMethodListenersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.InvokedMethodListenerSampleTestCase.class,
        InvokedMethodListenerHolder.LOGS,
        InvokedMethodListenerHolder.SUBSET,
        InvokedMethodListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForConfigurationListenersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.ConfigurationListenerSampleTestCase.class,
        ConfigurationListenerHolder.LOGS,
        ConfigurationListenerHolder.SUBSET,
        ConfigurationListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForClassListenersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.ClassListenerSampleTestCase.class,
        ClassListenerHolder.LOGS,
        ClassListenerHolder.SUBSET,
        ClassListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderListenersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.DataProviderListenerSampleTestCase.class,
        DataProviderListenerHolder.LOGS,
        DataProviderListenerHolder.SUBSET,
        DataProviderListenerHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForDataProviderInterceptorsViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.DataProviderInterceptorSampleTestCase.class,
        DataProviderInterceptorHolder.LOGS,
        DataProviderInterceptorHolder.SUBSET,
        DataProviderInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForExecutionVisualisersViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.ExecutionVisualiserSampleTestCase.class,
        ExecutionVisualiserHolder.LOGS,
        ExecutionVisualiserHolder.SUBSET,
        ExecutionVisualiserHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-2916")
  public void ensureOrderingForMethodInterceptorsViaAnnotation() {
    Ensure.orderingViaAnnotation(
        AnnotatedTestCaseSamplesHolder.MethodInterceptorSampleTestCase.class,
        MethodInterceptorHolder.LOGS,
        MethodInterceptorHolder.SUBSET,
        MethodInterceptorHolder.EXPECTED_LOGS);
  }

  @Test(description = "GITHUB-3064")
  public void ensureTestResultAttributesAreCarriedForward() {
    TestNG testng = create(SampleTestCase.class);
    testng.run();
    assertThat(EvidenceListener.failureTestResult.getAttribute(EvidenceListener.ATTRIBUTE_KEY))
        .isEqualTo("attributeValue");
  }

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

  @Test(description = "GITHUB-2752")
  public void testWiringInOfListenersInMultipleTestTagsWithNoListenerInSuite() {
    setupTest(false);
    List<String> expected =
        Arrays.asList(
            "onStart", "onBeforeClass", "onTestStart", "onTestSuccess", "onAfterClass", "onFinish");
    Map<String, List<String>> logs = ListenerSample.getLogs();
    assertThat(logs.get("Xml_Test_1")).containsAll(expected);
    assertThat(logs.get("Xml_Test_2")).containsAll(expected);
  }

  @Test(description = "GITHUB-2752")
  public void testWiringInOfListenersInMultipleTestTagsWithListenerInSuite() {
    setupTest(true);
    List<String> expected =
        Arrays.asList(
            "onStart", "onBeforeClass", "onTestStart", "onTestSuccess", "onAfterClass", "onFinish");
    Map<String, List<String>> logs = ListenerSample.getLogs();
    assertThat(logs.get("Xml_Test_1")).containsAll(expected);
    assertThat(logs.get("Xml_Test_2")).containsAll(expected);
  }

  @Test(description = "GITHUB-2771")
  public void testEnsureNativeListenersAreRunAlwaysAtEnd() {
    TestNG testng = create(TestCaseSample.class);
    testng.run();
    assertThat(testng.getStatus()).isEqualTo(ExitCode.FAILED);
  }

  @Test(description = "GITHUB-2880", dataProvider = "issue2880-dataprovider")
  public void testSkipStatusInBeforeAndAfterConfigurationMethod(
      Class<?> clazz, XmlSuite.FailurePolicy policy, List<String> expected) {
    TestNG tng = create(clazz);
    ListenerForIssue2880 listener = new ListenerForIssue2880();
    tng.setConfigFailurePolicy(policy);
    tng.addListener(listener);
    tng.run();
    assertThat(listener.getLogs()).containsExactlyElementsOf(expected);
  }

  private void setupTest(boolean addExplicitListener) {
    TestNG testng = new TestNG();
    XmlSuite xmlSuite = createXmlSuite("Xml_Suite");
    createXmlTest(xmlSuite, "Xml_Test_1", TestClassSample.class);
    createXmlTest(xmlSuite, "Xml_Test_2", TestClassSample.class);
    testng.setXmlSuites(Collections.singletonList(xmlSuite));
    testng.setVerbose(2);
    if (addExplicitListener) {
      ListenerSample listener = new ListenerSample();
      testng.addListener(listener);
    }
    testng.run();
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

  @DataProvider(name = "issue2880-dataprovider")
  public Object[][] getIssue2880Data() {
    List<String> passList =
        Arrays.asList(
            "BeforeInvocation_beforeClass_STARTED",
            "AfterInvocation_beforeClass_SUCCESS",
            "BeforeInvocation_beforeMethod_STARTED",
            "AfterInvocation_beforeMethod_SUCCESS",
            "BeforeInvocation_testMethod_STARTED",
            "AfterInvocation_testMethod_SUCCESS",
            "BeforeInvocation_afterMethod_STARTED",
            "AfterInvocation_afterMethod_SUCCESS",
            "BeforeInvocation_afterClass_STARTED",
            "AfterInvocation_afterClass_SUCCESS");

    List<String> skipList =
        Arrays.asList(
            "BeforeInvocation_beforeClass_STARTED",
            "AfterInvocation_beforeClass_FAILURE",
            "BeforeInvocation_beforeMethod_SKIP",
            "AfterInvocation_beforeMethod_SKIP",
            "BeforeInvocation_testMethod_SKIP",
            "AfterInvocation_testMethod_SKIP",
            "BeforeInvocation_afterMethod_SKIP",
            "AfterInvocation_afterMethod_SKIP",
            "BeforeInvocation_afterClass_SKIP",
            "AfterInvocation_afterClass_SKIP");

    List<String> failList =
        Arrays.asList(
            "BeforeInvocation_beforeClass_STARTED",
            "AfterInvocation_beforeClass_FAILURE",
            "BeforeInvocation_beforeMethod_SKIP",
            "AfterInvocation_beforeMethod_SKIP",
            "BeforeInvocation_testMethod_SKIP",
            "AfterInvocation_testMethod_SKIP",
            "BeforeInvocation_afterMethod_SKIP",
            "AfterInvocation_afterMethod_SKIP",
            "BeforeInvocation_afterClass_SKIP",
            "AfterInvocation_afterClass_SKIP");

    return new Object[][] {
      {TestClassWithPassingConfigsSample.class, XmlSuite.FailurePolicy.SKIP, passList},
      {TestClassWithFailingConfigsSample.class, XmlSuite.FailurePolicy.SKIP, skipList},
      {TestClassWithPassingConfigsSample.class, XmlSuite.FailurePolicy.CONTINUE, passList},
      {TestClassWithFailingConfigsSample.class, XmlSuite.FailurePolicy.CONTINUE, failList}
    };
  }

  private static class Ensure {

    private Ensure() {}

    static void orderingViaAnnotation(
        Class<?> testClass, List<String> logs, List<ITestNGListener> listeners, String[] expected) {
      logs.clear();
      TestNG testng = create(testClass);
      listeners.forEach(testng::addListener);
      testng.setUseDefaultListeners(false);
      testng.setListenerComparator(new AnnotationBackedListenerComparator());
      testng.run();
      assertThat(logs).containsExactly(expected);
    }

    static void orderingViaCli(List<String> logs, List<String> listeners, String[] expected) {
      orderingViaCli(NormalSampleTestCase.class, logs, listeners, expected);
    }

    static void orderingViaCli(
        Class<?> clazz, List<String> logs, List<String> listeners, String[] expected) {
      logs.clear();
      String[] args =
          new String[] {
            "-listener", String.join(",", listeners),
            "-usedefaultlisteners", "false",
            "-testclass", clazz.getName(),
            "-listenercomparator", AnnotationBackedListenerComparator.class.getName()
          };
      TestNG.privateMain(args, null);
      assertThat(logs).containsExactly(expected);
    }

    static void orderingViaXmlTag(List<String> logs, List<String> listeners, String[] expected) {
      orderingViaXmlTag(NormalSampleTestCase.class, logs, listeners, expected);
    }

    static void orderingViaXmlTag(
        Class<?> clazz, List<String> logs, List<String> listeners, String[] expected) {
      logs.clear();
      XmlSuite xmlSuite = createXmlSuite("suite", "test", clazz);
      listeners.forEach(xmlSuite::addListener);
      TestNG testng = create(xmlSuite);
      testng.setUseDefaultListeners(false);
      testng.setListenerComparator(new AnnotationBackedListenerComparator());
      testng.run();
      assertThat(logs).containsExactly(expected);
    }

    static void orderingViaApi(
        List<String> logs, List<ITestNGListener> listeners, String[] expected) {
      orderingViaApi(NormalSampleTestCase.class, logs, listeners, expected);
    }

    static void orderingViaApi(
        Class<?> clazz, List<String> logs, List<ITestNGListener> listeners, String[] expected) {
      logs.clear();
      TestNG testng = create(clazz);
      listeners.forEach(testng::addListener);
      testng.setUseDefaultListeners(false);
      testng.setListenerComparator(new AnnotationBackedListenerComparator());
      testng.run();
      assertThat(logs).containsExactly(expected);
    }
  }
}
