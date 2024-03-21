package test.support;

import static org.assertj.core.util.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Random;

public final class SafeRandoms {

  private static final Random random = new Random();

  private SafeRandoms() {}

  /**
   * @param delta - Represents a constant that should be added to the generated random value. This
   *     will ensure that there are no zero values generated.
   * @param upperBound - The upper bound (exclusive). Must be positive.
   * @return - A random number which is a summation of the delta and the actual random value that is
   *     lesser than the upperBound value.
   */
  public static int nextInt(int delta, int upperBound) {
    return nextInt(delta, upperBound, random);
  }

  /**
   * @param delta - Represents a constant that should be added to the generated random value. This
   *     will ensure that there are no zero values generated.
   * @param upperBound - The upper bound (exclusive). Must be positive.
   * @param random - An existing instance of {@link Random} to be used.
   * @return - A random number which is a summation of the delta and the actual random value that is
   *     lesser than the upperBound value.
   */
  public static int nextInt(int delta, int upperBound, Random random) {
    checkArgument(delta >= 0, "Delta should be non-zero");
    checkArgument(upperBound >= 0, "Upper bound should be non-zero");
    checkArgument(delta < upperBound, "Delta should be less than Upper bound");
    return delta + Objects.requireNonNull(random).nextInt(upperBound);
  }
}
