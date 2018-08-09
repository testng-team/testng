package test.listeners.ordering;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import org.testng.IAlterSuiteListener;
import org.testng.IAnnotationTransformer;
import org.testng.IClassListener;
import org.testng.IConfigurationListener;
import org.testng.IDataProviderListener;
import org.testng.IDataProviderMethod;
import org.testng.IExecutionListener;
import org.testng.IExecutionVisualiser;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

public class UniversalListener implements IExecutionListener, IAlterSuiteListener,
    ISuiteListener, ITestListener, IClassListener, IInvokedMethodListener, IDataProviderListener,
    IReporter, IExecutionVisualiser, IMethodInterceptor, IAnnotationTransformer,
    IConfigurationListener {

  private List<String> messages = Lists.newLinkedList();

  public List<String> getMessages() {
    return messages;
  }

  public void onConfigurationSuccess(ITestResult itr) {
    messages.add("org.testng.IConfigurationListener.onConfigurationSuccess(ITestResult itr)");
  }

  public void onConfigurationFailure(ITestResult itr) {
    messages.add("org.testng.IConfigurationListener.onConfigurationFailure(ITestResult itr)");
  }

  public void onConfigurationSkip(ITestResult itr) {
    messages.add("org.testng.IConfigurationListener.onConfigurationSkip(ITestResult itr)");
  }

  public void beforeConfiguration(ITestResult tr) {
    messages.add("org.testng.IConfigurationListener.beforeConfiguration(ITestResult tr)");
  }

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
      String outputDirectory) {
    messages.add(
        "org.testng.IReporter.generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory)");
  }

  @Override
  public void beforeDataProviderExecution(IDataProviderMethod dataProviderMethod,
      ITestNGMethod method, ITestContext iTestContext) {
    messages.add(
        "org.testng.IDataProviderListener.beforeDataProviderExecution(IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext)");

  }

  @Override
  public void afterDataProviderExecution(IDataProviderMethod dataProviderMethod,
      ITestNGMethod method, ITestContext iTestContext) {
    messages.add(
        "org.testng.IDataProviderListener.afterDataProviderExecution(IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext)");
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    messages.add(
        "org.testng.IInvokedMethodListener.beforeInvocation(IInvokedMethod method, ITestResult testResult)");
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult,
      ITestContext context) {
    messages.add(
        "org.testng.IInvokedMethodListener.beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context)");
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    messages.add(
        "org.testng.IInvokedMethodListener.afterInvocation(IInvokedMethod method, ITestResult testResult)");
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
    messages.add(
        "org.testng.IInvokedMethodListener.afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context)");
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    messages.add("org.testng.IClassListener.onAfterClass(ITestClass testClass)");
  }

  @Override
  public void onBeforeClass(ITestClass testClass) {
    messages.add("org.testng.IClassListener.onBeforeClass(ITestClass testClass)");
  }

  @Override
  public void alter(List<XmlSuite> suites) {
    messages.add("org.testng.IAlterSuiteListener.alter(List<XmlSuite> suites)");
  }

  @Override
  public void onExecutionStart() {
    messages.add("org.testng.IExecutionListener.onExecutionStart()");
  }

  @Override
  public void onExecutionFinish() {
    messages.add("org.testng.IExecutionListener.onExecutionFinish()");
  }

  @Override
  public void onStart(ISuite suite) {
    messages.add("org.testng.ISuiteListener.onStart()");
  }

  @Override
  public void onFinish(ISuite suite) {
    messages.add("org.testng.ISuiteListener.onFinish()");
  }

  @Override
  public void onStart(ITestContext context) {
    messages.add("org.testng.ITestListener.onStart(ITestContext context)");
  }

  @Override
  public void onFinish(ITestContext context) {
    messages.add("org.testng.ITestListener.onFinish(ITestContext context)");
  }

  @Override
  public void onTestStart(ITestResult result) {
    messages.add("org.testng.ITestListener.onTestStart(ITestResult result)");
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    messages.add("org.testng.ITestListener.onTestSuccess(ITestResult result)");
  }

  @Override
  public void onTestFailure(ITestResult result) {
    messages.add("org.testng.ITestListener.onTestFailure(ITestResult result)");
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    messages.add("org.testng.ITestListener.onTestSkipped(ITestResult result)");
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    messages
        .add("org.testng.ITestListener.onTestFailedButWithinSuccessPercentage(ITestResult result)");
  }

  @Override
  public void consumeDotDefinition(String dotDefinition) {
    messages.add("org.testng.IExecutionVisualiser.consumeDotDefinition(String dotDefinition)");
  }

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    String text = "org.testng.IMethodInterceptor.intercept(List<IMethodInstance> methods, ITestContext context)";
    messages.add(text);
    return methods;
  }

  @Override
  public void transform(IFactoryAnnotation annotation, Method method) {
    String text = "org.testng.IAnnotationTransformer.transform(IFactoryAnnotation annotation, Method method)";
    if (!messages.contains(text)) {
      messages.add(text);
    }
  }

  @Override
  public void transform(IListenersAnnotation annotation, Class testClass) {
    String text = "org.testng.IAnnotationTransformer.transform(IListenersAnnotation annotation, Class testClass)";
    if (!messages.contains(text)) {
      messages.add(text);
    }
  }

  @Override
  public void transform(IDataProviderAnnotation annotation, Method method) {
    String text = "org.testng.IAnnotationTransformer.transform(IDataProviderAnnotation annotation, Method method)";
    if (!messages.contains(text)) {
      messages.add(text);
    }
  }

  @Override
  public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
      Method testMethod) {
    String text = "org.testng.IAnnotationTransformer.transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)";
    if (!messages.contains(text)) {
      messages.add(text);
    }
  }

  @Override
  public void transform(IConfigurationAnnotation annotation, Class testClass,
      Constructor testConstructor, Method testMethod) {
    String text = "org.testng.IAnnotationTransformer.transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)";
    if (!messages.contains(text)) {
      messages.add(text);
    }
  }

}
