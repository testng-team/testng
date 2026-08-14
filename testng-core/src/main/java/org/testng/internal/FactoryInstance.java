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
    this.parameters = parameters;
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
   * @return - The invocation's parameters without copying them, for the callers that only read them
   *     and for {@link IParameterInfo#getParameters()}, whose contract predates the defensive copy
   *     {@link #getParameters()} makes.
   */
  Object[] rawParameters() {
    return parameters;
  }
}
