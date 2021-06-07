package test.thread.parallelization.issue2110;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.testng.annotations.Test;

public class TestClass {

  static Set<Long> threadIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

  public static Set<Long> getThreadIds() {
    return threadIds;
  }

  @Test
  public void test0() {
    threadIds.add(Thread.currentThread().getId());
  }

  @Test
  public void test1() {
    threadIds.add(Thread.currentThread().getId());
  }

  @Test(dependsOnMethods = "test1")
  public void test2() {
    threadIds.add(Thread.currentThread().getId());
  }

  @Test(dependsOnMethods = "test2")
  public void test3() {
    threadIds.add(Thread.currentThread().getId());
  }

  @Test
  public void test4() {
    threadIds.add(Thread.currentThread().getId());
  }
}
