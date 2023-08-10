package testhelper;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PerformanceUtils {

  private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
  private static final @Nullable Method GET_THREAD_ALLOCATED_BYTES = initGetThreadAllocatedBytes();

  private static @Nullable Method initGetThreadAllocatedBytes() {
    try {
      Method method = THREAD_MX_BEAN.getClass().getMethod("getThreadAllocatedBytes", long.class);
      method.setAccessible(true);
      method.invoke(THREAD_MX_BEAN, Thread.currentThread().getId());
      return method;
    } catch (Throwable e) {
      return null;
    }
  }

  public static boolean canMeasureAllocatedMemory() {
    return GET_THREAD_ALLOCATED_BYTES != null;
  }

  /** Returns amount of memory (in bytes) allocated by current thread until now. */
  public static long measureAllocatedMemory() {
    if (GET_THREAD_ALLOCATED_BYTES == null) {
      throw new IllegalStateException(
          "Method getThreadAllocatedBytes(long) is not found in ThreadMXBean " + THREAD_MX_BEAN);
    }
    long selfId = Thread.currentThread().getId();

    try {
      Object result = GET_THREAD_ALLOCATED_BYTES.invoke(THREAD_MX_BEAN, selfId);
      if (!(result instanceof Long)) {
        throw new IllegalStateException(
            "Method getThreadAllocatedBytes(long) returns unexpected type: " + result);
      }
      return (Long) result;
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to call " + GET_THREAD_ALLOCATED_BYTES, e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw new RuntimeException("Unable to call " + GET_THREAD_ALLOCATED_BYTES, cause);
    }
  }
}
