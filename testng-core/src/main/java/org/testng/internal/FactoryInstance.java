package org.testng.internal;

import org.testng.IFactory;
import org.testng.IFactoryInstance;

/**
 * The public view of one instance produced by a <code>&#64;Factory</code>, and the single place
 * holding that instance's factory metadata. {@link ParameterInfo} and {@link LazyParameterInfo}
 * delegate to it rather than duplicating the state.
 *
 * <p>Two indexes are recorded because two different questions are being asked:
 *
 * <ul>
 *   <li>{@code invocationIndex} -- the position of the factory <em>invocation</em> this instance
 *       came out of. That is what {@link IParameterInfo#getIndex()} has always returned, and it is
 *       kept unchanged.
 *   <li>{@code slot} -- the position of this instance inside that invocation's output. Always
 *       {@code 0} for a constructor factory, which produces one instance per invocation.
 * </ul>
 *
 * <p>{@link #getIndex()} is their sum: the instance's position in the factory's whole output.
 */
public final class FactoryInstance implements IFactoryInstance {

  private final int invocationIndex;
  private final int slot;
  private final Object[] parameters;
  private final IFactory factory;

  FactoryInstance(int invocationIndex, int slot, Object[] parameters, IFactory factory) {
    this.invocationIndex = invocationIndex;
    this.slot = slot;
    // Snapshot: an Iterator<Object[]> data provider is free to hand back the same array for every
    // row (a reused buffer), and this outlives the invocation -- getParameters() is read from a
    // listener, long after the provider has moved on. Without the copy every instance of such a
    // factory would report the last row.
    this.parameters = parameters.clone();
    this.factory = factory;
  }

  @Override
  public int getIndex() {
    return invocationIndex + slot;
  }

  @Override
  public Object[] getParameters() {
    return parameters.clone();
  }

  @Override
  public IFactory getFactory() {
    return factory;
  }

  /** @return - The index of the factory invocation that produced this instance. */
  int getInvocationIndex() {
    return invocationIndex;
  }

  /**
   * @return - This instance's snapshot of the invocation parameters, without the further copy
   *     {@link #getParameters()} hands to callers. Backs {@link IParameterInfo#getParameters()},
   *     whose contract predates that defensive copy.
   */
  Object[] rawParameters() {
    return parameters;
  }
}
