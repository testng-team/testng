package testhelper;

import com.sun.management.ThreadMXBean;

import java.lang.management.ManagementFactory;

public class PerformanceUtils {

  private static ThreadMXBean THREAD_MX_BEAN;

  static {
    Object bean = ManagementFactory.getThreadMXBean();
    if (bean instanceof ThreadMXBean)
      THREAD_MX_BEAN = (ThreadMXBean) bean;
  }

  /**
   * @return amount of memory (in bytes) allocated by current thread until now
   */
  public static long measureAllocatedMemory() {
    if (THREAD_MX_BEAN == null) {
      throw new IllegalStateException("Couldn't get thread MBean");
    }
    long selfId = Thread.currentThread().getId();

    return THREAD_MX_BEAN.getThreadAllocatedBytes(selfId);
  }
}
