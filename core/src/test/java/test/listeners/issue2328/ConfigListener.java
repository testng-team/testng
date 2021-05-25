package test.listeners.issue2328;

import java.util.HashMap;
import java.util.Map;
import org.testng.IConfigurationListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

public class ConfigListener implements IConfigurationListener {

  private final Map<String, ITestNGMethod> data = new HashMap<>();

  public ITestNGMethod getDataFor(String key) {
    return data.get(key);
  }

  @Override
  public void beforeConfiguration(ITestResult tr, ITestNGMethod tm) {
    data.put("before", tm);
  }

  @Override
  public void onConfigurationFailure(ITestResult tr, ITestNGMethod tm) {
    data.put("failed", tm);
  }

  @Override
  public void onConfigurationSkip(ITestResult tr, ITestNGMethod tm) {
    data.put("skip", tm);
  }

  @Override
  public void onConfigurationSuccess(ITestResult tr, ITestNGMethod tm) {
    data.put("success", tm);
  }
}
