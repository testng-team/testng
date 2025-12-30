package test.retryAnalyzer.issue3231.samples;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MutationSample {

  public static class Custom {
    private UUID id;

    /**
     * Creates a Custom instance with the given UUID as its identity.
     *
     * @param id the UUID to assign to this instance; used as the basis for {@code equals} and {@code hashCode}
     */
    public Custom(UUID id) {
      this.id = id;
    }

    /**
     * Gets the UUID identifying this Custom instance.
     *
     * @return the UUID used as this object's identifier
     */
    public UUID getId() {
      return id;
    }

    /**
     * Sets the identifier for this Custom instance.
     *
     * @param id the new UUID identifier, may be {@code null}
     */
    public void setId(UUID id) {
      this.id = id;
    }

    /**
     * Compares this Custom instance to another object for equality based on the `id` field.
     *
     * @param o the object to compare with
     * @return `true` if the specified object is a `Custom` whose `id` is equal to this instance's `id`, `false` otherwise
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Custom custom = (Custom) o;
      return Objects.equals(id, custom.id);
    }

    /**
     * Computes the hash code for this Custom instance using its `id` field.
     *
     * @return the hash code derived from the `id` field
     */
    @Override
    public int hashCode() {
      return Objects.hash(id);
    }
  }

  public static class MyRetry implements IRetryAnalyzer {
    private int retryCount = 0;
    private int maxRetryCount = 3;

    /**
     * Decides whether a failed test should be retried up to the configured maximum.
     *
     * Increments the internal retry counter when a retry is allowed.
     *
     * @param result the test result for the current execution
     * @return `true` if the test should be retried, `false` otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
      if (retryCount < maxRetryCount) {
        retryCount++;
        return true;
      }
      return false;
    }
  }

  public static final AtomicInteger guardCounter = new AtomicInteger(0);

  /**
   * Provides test data containing a single Custom instance initialized with a random UUID.
   *
   * @return a two-dimensional Object array with one row; the row contains a single `Custom` instance
   *         constructed with a newly generated `UUID`
   */
  @DataProvider(name = "dpCustomObject")
  public Object[][] dpCustomObject() {
    return new Object[][] {{new Custom(UUID.randomUUID())}};
  }

  /**
   * Test that mutates the supplied Custom instance, always fails, and is intended to exercise retry behavior.
   *
   * The method increments a global guard counter and returns early if the counter exceeds 100 to prevent excessive invocations.
   *
   * @param newObject the Custom instance provided by the data provider; its `id` field is replaced with a new UUID before the test fails
   */
  @Test(dataProvider = "dpCustomObject", retryAnalyzer = MyRetry.class)
  public void willNotStopAfter3FailuresCustom(Custom newObject) {
    if (guardCounter.incrementAndGet() > 100) {
      return;
    }
    newObject.setId(UUID.randomUUID()); // Mutate!
    Assert.fail();
  }
}