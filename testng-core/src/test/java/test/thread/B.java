package test.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.Test;

public class B {
  public static Map<Long, Long> m_threadIds = new ConcurrentHashMap<>();

  public static void setUp() {
    m_threadIds = new ConcurrentHashMap<>();
  }

  @Test
  public void f2() {
    Long id = Thread.currentThread().getId();
    m_threadIds.put(id, id);
  }
}
