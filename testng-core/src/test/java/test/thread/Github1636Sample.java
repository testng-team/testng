package test.thread;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.Test;

public class Github1636Sample {
  private static Map<Long, Boolean> map = new ConcurrentHashMap<>();
  static Set<Long> threads = Collections.newSetFromMap(map);

  @Test
  public void test1() {
    threads.add(Thread.currentThread().getId());
  }

  @Test
  public void test2() {
    threads.add(Thread.currentThread().getId());
  }

  @Test
  public void test3() {
    threads.add(Thread.currentThread().getId());
  }
}
