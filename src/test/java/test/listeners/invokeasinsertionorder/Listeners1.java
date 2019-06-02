package test.listeners.invokeasinsertionorder;

import org.testng.IConfigurationListener;
import org.testng.IExecutionListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class Listeners1 implements IExecutionListener, ITestListener, IInvokedMethodListener, IConfigurationListener {

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    GetMessages.getMessages().add("1_org.testng.IConfigurationListener.onConfigurationSuccess(ITestResult itr)");
  }

  @Override
  public void onConfigurationFailure(ITestResult itr) {
    GetMessages.getMessages().add("1_org.testng.IConfigurationListener.onConfigurationFailure(ITestResult itr)");
  }

  @Override
  public void onConfigurationSkip(ITestResult itr) {
    GetMessages.getMessages().add("1_org.testng.IConfigurationListener.onConfigurationSkip(ITestResult itr)");
  }

  @Override
  public void beforeConfiguration(ITestResult tr) {
    GetMessages.getMessages().add("1_org.testng.IConfigurationListener.beforeConfiguration(ITestResult tr)");
  }


  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    GetMessages.getMessages().add(
        "1_org.testng.IInvokedMethodListener.beforeInvocation(IInvokedMethod method, ITestResult testResult)");
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    GetMessages.getMessages().add(
        "1_org.testng.IInvokedMethodListener.afterInvocation(IInvokedMethod method, ITestResult testResult)");
  }
  
  @Override
  public void onExecutionStart() {
    GetMessages.getMessages().add("1_org.testng.IExecutionListener.onExecutionStart()");
  }

  @Override
  public void onExecutionFinish() {
    GetMessages.getMessages().add("1_org.testng.IExecutionListener.onExecutionFinish()");
  }

  @Override
  public void onStart(ITestContext context) {
    GetMessages.getMessages().add("1_org.testng.ITestListener.onStart(ITestContext context)");
  }

  @Override
  public void onFinish(ITestContext context) {
    GetMessages.getMessages().add("1_org.testng.ITestListener.onFinish(ITestContext context)");
  }

  @Override
  public void onTestStart(ITestResult result) {
    GetMessages.getMessages().add("1_org.testng.ITestListener.onTestStart(ITestResult result)");
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    GetMessages.getMessages().add("1_org.testng.ITestListener.onTestSuccess(ITestResult result)");
  }

  @Override
  public void onTestFailure(ITestResult result) {
    GetMessages.getMessages().add("1_org.testng.ITestListener.onTestFailure(ITestResult result)");
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    GetMessages.getMessages().add("1_org.testng.ITestListener.onTestSkipped(ITestResult result)");
  }


}
