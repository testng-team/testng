package test.listeners.ordering;

public interface Constants {

  String IALTERSUITELISTENER_ALTER = "org.testng.IAlterSuiteListener.alter(List<XmlSuite> suites)";
  String IANNOTATIONTRANSFORMER_TRANSFORM_3_ARGS =
      "org.testng.IAnnotationTransformer.transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)";
  String IANNOTATIONTRANSFORMER_TRANSFORM_4_ARGS =
      "org.testng.IAnnotationTransformer.transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)";
  String IANNOTATIONTRANSFORMER_DATAPROVIDER =
      "org.testng.IAnnotationTransformer.transform(IDataProviderAnnotation annotation, Method method)";
  String IANNOTATIONTRANSFORMER_FACTORY =
      "org.testng.IAnnotationTransformer.transform(IFactoryAnnotation annotation, Method method)";
  String METHODINTERCEPTOR_INTERCEPT =
      "org.testng.IMethodInterceptor.intercept(List<IMethodInstance> methods, ITestContext context)";
  String IEXECUTION_VISUALISER_CONSUME_DOT_DEFINITION =
      "org.testng.IExecutionVisualiser.consumeDotDefinition(String dotDefinition)";
  String IREPORTER_GENERATE_REPORT =
      "org.testng.IReporter.generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory)";

  String ISUITELISTENER_ON_START = "org.testng.ISuiteListener.onStart()";
  String ISUITELISTENER_ON_FINISH = "org.testng.ISuiteListener.onFinish()";

  String ITESTLISTENER_ON_START_TEST_METHOD =
      "org.testng.ITestListener.onTestStart(ITestResult result)";
  String ITESTLISTENER_ON_TEST_FAILURE_TEST_METHOD =
      "org.testng.ITestListener.onTestFailure(ITestResult result)";
  String ITESTLISTENER_ON_TEST_TIMEOUT_TEST_METHOD =
      "org.testng.ITestListener.onTestFailedWithTimeout(ITestResult result)";
  String ITESTLISTENER_ON_TEST_SUCCESS_TEST_METHOD =
      "org.testng.ITestListener.onTestSuccess(ITestResult result)";
  String ITESTLISTENER_ON_TEST_SKIPPED_TEST_METHOD =
      "org.testng.ITestListener.onTestSkipped(ITestResult result)";

  String ITESTLISTENER_ON_START_TEST_TAG = "org.testng.ITestListener.onStart(ITestContext context)";
  String ITESTLISTENER_ON_FINISH_TEST_TAG =
      "org.testng.ITestListener.onFinish(ITestContext context)";

  String ICLASSLISTENER_ON_BEFORE_CLASS =
      "org.testng.IClassListener.onBeforeClass(ITestClass testClass)";
  String ICLASSLISTENER_ON_AFTER_CLASS =
      "org.testng.IClassListener.onAfterClass(ITestClass testClass)";

  String IINVOKEDMETHODLISTENER_BEFORE_INVOCATION =
      "org.testng.IInvokedMethodListener.beforeInvocation(IInvokedMethod method, ITestResult testResult)";
  String IINVOKEDMETHODLISTENER_BEFORE_INVOCATION_WITH_CONTEXT =
      "org.testng.IInvokedMethodListener.beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context)";
  String IINVOKEDMETHODLISTENER_AFTER_INVOCATION =
      "org.testng.IInvokedMethodListener.afterInvocation(IInvokedMethod method, ITestResult testResult)";
  String IINVOKEDMETHODLISTENER_AFTER_INVOCATION_WITH_CONTEXT =
      "org.testng.IInvokedMethodListener.afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context)";

  String IEXECUTIONLISTENER_ON_EXECUTION_START = "org.testng.IExecutionListener.onExecutionStart()";
  String IEXECUTIONLISTENER_ON_EXECUTION_FINISH =
      "org.testng.IExecutionListener.onExecutionFinish()";

  String IDATAPROVIDERLISTENER_BEFORE_DATA_PROVIDER_EXECUTION =
      "org.testng.IDataProviderListener.beforeDataProviderExecution(IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext)";
  String IDATAPROVIDERLISTENER_AFTER_DATA_PROVIDER_EXECUTION =
      "org.testng.IDataProviderListener.afterDataProviderExecution(IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext)";

  String ICONFIGURATIONLISTENER_BEFORE_CONFIGURATION =
      "org.testng.IConfigurationListener.beforeConfiguration(ITestResult tr)";
  String ICONFIGURATIONLISTENER_ON_CONFIGURATION_SUCCESS =
      "org.testng.IConfigurationListener.onConfigurationSuccess(ITestResult itr)";
  String ICONFIGURATIONLISTENER_ON_CONFIGURATION_FAILURE =
      "org.testng.IConfigurationListener.onConfigurationFailure(ITestResult itr)";
  String ICONFIGURATIONLISTENER_ON_CONFIGURATION_SKIP =
      "org.testng.IConfigurationListener.onConfigurationSkip(ITestResult itr)";
}
