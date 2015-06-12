package org.testng.asserts;

import org.testng.collections.Lists;

import java.util.List;

/**
 * Log the messages of all the assertions that get run.
 */
public class LoggingAssert extends Assertion {

  private List<String> m_messages = Lists.newArrayList();

  @Override
  public void onBeforeAssert(IAssert<?> a) {
    m_messages.add("Test:" + a.getMessage());
  }

  public List<String> getMessages() {
    return m_messages;
  }
}
