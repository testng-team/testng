package testhelper;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

public class PerformanceUtils {

  private static final ThreadMXBean THREAD_MX_BEAN = initThreadBean();

  private static ThreadMXBean initThreadBean() {
    Object bean = ManagementFactory.getThreadMXBean();
    return bean instanceof ThreadMXBean ? (ThreadMXBean) bean : null;
  }

  /** @return amount of memory (in bytes) allocated by current thread until now */
  public static long measureAllocatedMemory() {
    if (THREAD_MX_BEAN == null) {
      throw new IllegalStateException("Couldn't get thread MBean");
    }
    long selfId = Thread.currentThread().getId();

    return THREAD_MX_BEAN.getThreadAllocatedBytes(selfId);
  }
}
