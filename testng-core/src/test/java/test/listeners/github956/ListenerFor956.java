package test.listeners.github956;

import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class ListenerFor956 implements ITestListener {
  private static final List<String> messages = Lists.newArrayList();

  public static List<String> getMessages() {
    return messages;
  }

  @Override
  public void onStart(ITestContext context) {
    messages.add("Executing " + context.getCurrentXmlTest().getName());
  }

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {}

  @Override
  public void onTestFailure(ITestResult result) {}

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  @Override
  public void onFinish(ITestContext context) {}
}
