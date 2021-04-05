package test.listeners;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.List;

public class SimpleListener extends TestListenerAdapter {
  public static List<Integer> m_list;

  @Override
  public void onTestSuccess(ITestResult tr) {
    m_list.add(3);
    super.onTestSuccess(tr);
  }

  @Override
  public void onTestFailure(ITestResult tr) {
    m_list.add(5);
    super.onTestSuccess(tr);
  }
}
