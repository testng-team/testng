package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.KeyAwareAutoCloseableLock;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;

/**
 * Test that classes with dependent methods are still run in different threads and in the correct
 * order.
 */
public class MultiThreadedDependentTest extends SimpleBaseTest {

  /**
   * Make sure that the topological order is preserved and that if the TestNG runner is configured
   * to run n threads, the dependent methods are using these n threads.
   */
  private void assertOrder(List<String> methods) {
    List<String> expectedMethods =
        Arrays.asList(
            "a1", "a2", "a3", "b1", "b2", "b3", "b4", "b5", "c1", "d", "x", "y", "z", "t");
    int size = expectedMethods.size();
    assertThat(methods).hasSize(size);
    for (String em : expectedMethods) {
      assertThat(methods).contains(em);
    }
    Map<String, Boolean> map = Maps.newHashMap();
    for (String m : methods) {
      map.put(m, Boolean.TRUE);
      if ("b1".equals(m) || "b2".equals(m) || "b3".equals(m) || "b4".equals(m) || "b5".equals(m)) {
        assertThat(map.get("a1")).isTrue();
        assertThat(map.get("a2")).isTrue();
        assertThat(map.get("a3")).isTrue();
      }
      if ("d".equals(m)) {
        assertThat(map.get("a1")).isTrue();
        assertThat(map.get("a2")).isTrue();
      }
      if ("c1".equals(m)) {
        assertThat(map.get("b1")).isTrue();
        assertThat(map.get("b2")).isTrue();
      }
    }
    assertThat(map).hasSize(size);
    for (Boolean val : map.values()) {
      assertThat(val).isTrue();
    }
  }

  @Test
  public void test2Threads() {
    test(2);
  }

  @Test
  public void test3Threads() {
    test(3);
  }

  private final KeyAwareAutoCloseableLock lock = new KeyAwareAutoCloseableLock();

  private void test(int threadCount) {
    Helper.reset();
    MultiThreadedDependentSampleTest.m_methods = Lists.newArrayList();
    TestNG tng = create(MultiThreadedDependentSampleTest.class);
    tng.setThreadCount(threadCount);
    tng.setParallel(XmlSuite.ParallelMode.METHODS);
    Map<Long, Long> map = Helper.getMap(MultiThreadedDependentSampleTest.class.getName());
    try (KeyAwareAutoCloseableLock.AutoReleasable ignore = lock.lockForObject(map)) {
      tng.run();
      assertThat(map)
          .withFailMessage("Map size:" + map.size() + " expected more than 1")
          .hasSizeGreaterThan(1);
      assertOrder(MultiThreadedDependentSampleTest.m_methods);
    }
  }
}
