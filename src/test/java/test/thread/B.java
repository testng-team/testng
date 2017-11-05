package test.thread;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.testng.collections.Maps;

public class B {
  public static Map<Long, Long> m_threadIds = Maps.newConcurrentMap();

  public static void setUp() {
    m_threadIds = Maps.newConcurrentMap();
  }

  @Test
  public void f2() {
    Long id = Thread.currentThread().getId();
    m_threadIds.put(id, id);
  }

  private static void ppp(String s) {
    System.out.println("[FactoryTest] " + s);
  }

}
