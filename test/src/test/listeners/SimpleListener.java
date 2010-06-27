package test.listeners;

import java.util.List;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class SimpleListener extends TestListenerAdapter {
  public static List<Integer> m_list;

  public void onTestSuccess(ITestResult tr) {
    SimpleListener.m_list.add(3);
    super.onTestSuccess(tr);
  }

}
