package test.thread;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test for test level thread-count.
 *
 * @author <a href="mailto:the.mindstorm@gmail.com">Alex Popescu</a>
 */
public class TestThreadCountTest {
  private Set<String> m_threads= Collections.synchronizedSet(new HashSet<String>());

  @Test
  public void test1() {
    m_threads.add(ThreadUtil.currentThreadInfo());
  }

  @Test
  public void test2() {
    m_threads.add(ThreadUtil.currentThreadInfo());
  }

  @Test
  public void test3() {
    m_threads.add(ThreadUtil.currentThreadInfo());
  }

  @AfterClass
  public void checkThreading() {
    Assert.assertEquals(m_threads.size(), 3, "Test should use 3 threads");
  }
}
