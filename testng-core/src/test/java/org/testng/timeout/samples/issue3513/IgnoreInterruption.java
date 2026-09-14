package org.testng.timeout.samples.issue3513;

/**
 * Spins past a suite time-out of a few hundred milliseconds, and swallows interruption. Samples
 * that ignore cancellation share this wait so the overrun length stays in one place.
 */
final class IgnoreInterruption {

  private IgnoreInterruption() {}

  static void forMillis(long millis) {
    long end = System.currentTimeMillis() + millis;
    while (System.currentTimeMillis() < end) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException ignored) {
        // Keep going, as a method that ignores interruption does.
      }
    }
  }
}
