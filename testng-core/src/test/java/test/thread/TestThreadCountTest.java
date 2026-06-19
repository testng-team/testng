package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadUtil;

/**
 * Test for test level thread-count.
 *
 * @author <a href="mailto:the.mindstorm@gmail.com">Alex Popescu</a>
 */
public class TestThreadCountTest {
  private Set<String> m_threads = Collections.synchronizedSet(new HashSet<String>());

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
    assertThat(m_threads).withFailMessage("Test should use 3 threads").hasSize(3);
  }
}
