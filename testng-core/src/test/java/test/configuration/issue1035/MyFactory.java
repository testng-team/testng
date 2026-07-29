package test.configuration.issue1035;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import org.testng.annotations.Factory;

public class MyFactory {

  public static final int INSTANCE_COUNT = 5;

  public static final List<Long> THREAD_IDS = new CopyOnWriteArrayList<>();

  private static final int BARRIER_TIMEOUT_SECONDS = 10;

  private static volatile CyclicBarrier barrier = new CyclicBarrier(INSTANCE_COUNT);

  public static void reset() {
    THREAD_IDS.clear();
    barrier = new CyclicBarrier(INSTANCE_COUNT);
  }

  /**
   * Records the current thread and then blocks until every other instance has reached this same
   * point. The barrier can only be crossed when all the {@code @BeforeClass} methods overlap in
   * time, which is what GITHUB-1035 is about. A sequential execution makes the first wait time out,
   * which breaks the barrier for the remaining instances.
   */
  static void recordAndAwaitPeers() throws InterruptedException {
    THREAD_IDS.add(Thread.currentThread().getId());
    try {
      barrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (TimeoutException | BrokenBarrierException e) {
      throw new IllegalStateException(
          "@BeforeClass methods did not run concurrently. Threads so far: " + THREAD_IDS, e);
    }
  }

  @Factory
  public Object[] instances() {
    return Stream.generate(TestclassExample::new).limit(INSTANCE_COUNT).toArray();
  }
}
