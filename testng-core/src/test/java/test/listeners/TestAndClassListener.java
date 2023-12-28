package test.listeners;

import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestListener;

public class TestAndClassListener implements ITestListener, IClassListener {

  private int beforeClassCount = 0;
  private int afterClassCount = 0;

  @Override
  public void onBeforeClass(ITestClass testClass) {
    beforeClassCount++;
  }

  @Override
  public void onAfterClass(ITestClass testClass) {
    afterClassCount++;
  }

  public int getBeforeClassCount() {
    return beforeClassCount;
  }

  public int getAfterClassCount() {
    return afterClassCount;
  }
}
