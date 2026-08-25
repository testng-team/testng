package test.aftergroups.issue1880;

import java.util.ArrayList;
import java.util.List;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

public class LocalConfigListener implements IConfigurationListener {
  private final List<String> messages = new ArrayList<>();

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    messages.add(itr.getMethod().getMethodName());
  }

  public List<String> getMessages() {
    return messages;
  }
}
