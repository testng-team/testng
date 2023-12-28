package test.listeners.github956;

import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.collections.Lists;

public class ListenerFor956 implements ITestListener {
  private static final List<String> messages = Lists.newArrayList();

  public static List<String> getMessages() {
    return messages;
  }

  @Override
  public void onStart(ITestContext context) {
    messages.add("Executing " + context.getCurrentXmlTest().getName());
  }
}
