package test.listeners;

import org.testng.IClassListener;
import org.testng.IMethodInstance;
import org.testng.ITestClass;

import java.util.ArrayList;
import java.util.List;

public class MyClassListener implements IClassListener {

  public static final List<String> beforeNames = new ArrayList<>();
  public static final List<String> afterNames = new ArrayList<>();

  @Override
  public void onBeforeClass(ITestClass testClass, IMethodInstance mi) {
    beforeNames.add(testClass.getRealClass().getSimpleName());
  }

  @Override
  public void onAfterClass(ITestClass testClass, IMethodInstance mi) {
    afterNames.add(testClass.getRealClass().getSimpleName());
  }
}
