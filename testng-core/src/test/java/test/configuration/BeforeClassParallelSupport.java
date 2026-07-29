package test.configuration;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared rendezvous used by {@link BeforeClassThreadA} and {@link BeforeClassThreadB} to prove that
 * their {@code @BeforeClass} methods overlap in time, without relying on timestamps.
 */
final class BeforeClassParallelSupport {

  /** The barrier is sized from this list, so adding a sample cannot desynchronize it. */
  static final Class<?>[] SAMPLES = {BeforeClassThreadA.class, BeforeClassThreadB.class};

  static final int PARTICIPANTS = SAMPLES.length;

  private static final int BARRIER_TIMEOUT_SECONDS = 10;

  private static final AtomicInteger arrivals = new AtomicInteger();
  private static volatile CyclicBarrier barrier = new CyclicBarrier(PARTICIPANTS);

  private BeforeClassParallelSupport() {}

  static void reset() {
    arrivals.set(0);
    barrier = new CyclicBarrier(PARTICIPANTS);
  }

  /** Number of {@code @BeforeClass} methods that reached the rendezvous. */
  static int getArrivals() {
    return arrivals.get();
  }

  /** Blocks until the peer {@code @BeforeClass} method reaches this same point. */
  static void awaitPeer() throws InterruptedException {
    arrivals.incrementAndGet();
    try {
      barrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (TimeoutException | BrokenBarrierException e) {
      throw new IllegalStateException("@BeforeClass methods did not run concurrently", e);
    }
  }
}
