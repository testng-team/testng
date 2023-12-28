package test.verify;

import org.testng.ITestContext;
import org.testng.ITestListener;

public class VerifyTestListener implements ITestListener {
  public static int m_count = 0;

  @Override
  public void onStart(ITestContext context) {
    m_count++;
  }
}
