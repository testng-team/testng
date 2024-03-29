package test.listeners.github1130;

import java.util.ArrayList;
import java.util.List;
import org.testng.IClassListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;

public class MyListener implements ISuiteListener, IClassListener {

  public static MyListener instance;
  public List<String> beforeSuiteCount = new ArrayList<>();
  public List<String> beforeClassCount = new ArrayList<>();

  public MyListener() {
    if (instance == null) {
      instance = this;
    }
  }

  public void onStart(ISuite suite) {
    beforeSuiteCount.add(this.toString());
  }

  public void onBeforeClass(ITestClass testClass) {
    beforeClassCount.add(this.toString());
  }
}
