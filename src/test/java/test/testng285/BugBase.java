package test.testng285;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

@Test(sequential = true)
public class BugBase {
  static Set<Long> m_threadIds;

  @BeforeClass
  public void setup() {
    m_threadIds = new HashSet<>();
  }

  void log(long threadId) {
    m_threadIds.add(threadId);
  }

  public void fbase() {
    log(Thread.currentThread().getId());
  }

}
