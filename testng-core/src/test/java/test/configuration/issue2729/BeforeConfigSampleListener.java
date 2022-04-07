package test.configuration.issue2729;

import org.testng.IConfigurationListener;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class BeforeConfigSampleListener
    implements ITestListener, IInvokedMethodListener, IConfigurationListener {
  public static int count = 0;

  @Override
  public void beforeConfiguration(ITestResult testResult) {
    count++;
  }
}
