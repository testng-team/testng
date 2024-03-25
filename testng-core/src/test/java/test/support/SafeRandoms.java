package test.support;

import java.util.Objects;
import java.util.Random;

public final class SafeRandoms {

  private static final Random random = new Random();

  private SafeRandoms() {}

  /**
   * @param bound - The upper bound (exclusive). Must be positive.
   * @return - the next pseudorandom, uniformly distributed int value between 1 and bound
   *     (exclusive) from this random number generator's sequence
   */
  public static int nextInt(int bound) {
    return nextInt(random, bound);
  }

  /**
   * @param random - An existing instance of {@link Random} to be used.
   * @param bound - The upper bound (exclusive). Must be positive.
   * @return - the next pseudorandom, uniformly distributed int value between 1 and bound
   *     (exclusive) from this random number generator's sequence
   */
  public static int nextInt(Random random, int bound) {
    if (bound <= 0) {
      throw new IllegalStateException("The upper bound should be positive number.");
    }
    int value = 0;
    while (value == 0) {
      value = Objects.requireNonNull(random).nextInt(bound);
    }
    return value;
  }
}
