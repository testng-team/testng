package test.beforegroups.issue2359;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class ListenerAdapter extends TestListenerAdapter {

  private final Collection<ITestResult> passedConfiguration = new ConcurrentLinkedQueue<>();

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    super.onConfigurationSuccess(itr);
    this.passedConfiguration.add(itr);
  }

  public Collection<ITestResult> getPassedConfiguration() {
    return passedConfiguration;
  }
}
