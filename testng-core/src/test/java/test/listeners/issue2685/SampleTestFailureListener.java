package test.listeners.issue2685;

import java.util.LinkedList;
import java.util.List;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class SampleTestFailureListener implements ITestListener {

  private final List<String> interruptedMethods = new LinkedList<>();

  @Override
  public void onTestFailure(ITestResult result) {
    if (Thread.currentThread().isInterrupted()) {
      interruptedMethods.add(result.getMethod().getQualifiedName());
    }
  }

  public List<String> getInterruptedMethods() {
    return interruptedMethods;
  }
}
