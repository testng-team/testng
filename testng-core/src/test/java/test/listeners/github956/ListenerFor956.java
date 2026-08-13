package test.listeners.github956;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestContext;
import org.testng.ITestListener;

public class ListenerFor956 implements ITestListener {
  private static final List<String> messages = new ArrayList<>();

  public static List<String> getMessages() {
    return messages;
  }

  @Override
  public void onStart(ITestContext context) {
    messages.add("Executing " + context.getCurrentXmlTest().getName());
  }
}
