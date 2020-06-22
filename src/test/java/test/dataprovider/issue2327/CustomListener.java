package test.dataprovider.issue2327;

import java.util.Arrays;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class CustomListener implements ITestListener {

  @Override
  public void onTestSuccess(ITestResult testResult) {

    System.out.println("Success: " + Arrays.toString(testResult.getParameters()));
  }

  @Override
  public void onTestSkipped(ITestResult testResult) {

    System.out.println("Skipped: " + Arrays.toString(testResult.getParameters()));
  }
}
