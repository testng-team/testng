package test.aftergroups.issue1880;

import java.util.List;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;
import org.testng.collections.Lists;

public class LocalConfigListener implements IConfigurationListener {
  private List<String> messages = Lists.newLinkedList();

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    messages.add(itr.getMethod().getMethodName());
  }

  public List<String> getMessages() {
    return messages;
  }
}
