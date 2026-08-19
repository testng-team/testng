package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.testng.internal.MethodInstanceTest.TestNGMethodStub;

/**
 * {@link MethodSorting#INSTANCES} used to fall through to a hash code comparison for every pair,
 * because its identity branch tested the wrong values.
 *
 * <p>These do not distinguish the two: the ids are random UUIDs, so comparing them and comparing
 * their hash codes both produce an arbitrary order, and the hash collision that would tell them
 * apart is not reachable from a test. What they pin is the contract the identity branch restores
 * unconditionally and the hash comparison only kept by luck -- two different instances are ordered
 * rather than tied, and the answer does not depend on which one is asked first.
 */
public class MethodSortingTest {

  @Test(description = "Two methods carrying different instance ids are strictly ordered")
  public void instancesOfTheSameMethodAreOrderedByTheirIdentity() {
    ITestNGMethod one = new IdentifiableStub(UUID.randomUUID());
    ITestNGMethod two = new IdentifiableStub(UUID.randomUUID());

    assertThat(MethodSorting.INSTANCES.compare(one, two)).isNotZero();
  }

  @Test(description = "The order two methods are compared in does not change the answer")
  public void theComparisonIsAntisymmetric() {
    ITestNGMethod one = new IdentifiableStub(UUID.randomUUID());
    ITestNGMethod two = new IdentifiableStub(UUID.randomUUID());

    assertThat(MethodSorting.INSTANCES.compare(one, two))
        .isEqualTo(-MethodSorting.INSTANCES.compare(two, one));
  }

  @Test(description = "A method is tied with itself")
  public void aMethodIsTiedWithItself() {
    ITestNGMethod only = new IdentifiableStub(UUID.randomUUID());

    assertThat(MethodSorting.INSTANCES.compare(only, only)).isZero();
  }

  /**
   * Everything the comparator reads before it reaches the identity is deliberately equal between
   * two of these, so that the identity is what decides.
   */
  private static class IdentifiableStub extends TestNGMethodStub implements IInstanceIdentity {

    private final UUID instanceId;

    IdentifiableStub(UUID instanceId) {
      super("sample", null);
      this.instanceId = instanceId;
    }

    @Override
    public UUID getInstanceId() {
      return instanceId;
    }

    @Override
    public Class<?> getRealClass() {
      return MethodSortingTest.class;
    }

    @Override
    public String toString() {
      return "IdentifiableStub";
    }
  }
}
