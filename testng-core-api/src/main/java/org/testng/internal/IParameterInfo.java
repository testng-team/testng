package org.testng.internal;

import org.jspecify.annotations.Nullable;
import org.testng.IFactoryInstance;

/** Represents the ability to retrieve the parameters associated with a factory method. */
public interface IParameterInfo {

  /**
   * @return - The actual instance associated with a factory method, or <code>null</code> if a lazy
   *     implementation's construction failed -- the failure is memoized rather than rethrown, and
   *     {@link #getInstantiationFailure()} reports it.
   */
  @Nullable
  Object getInstance();

  /**
   * @return - The index of the factory <em>invocation</em> that produced this instance. A factory
   *     method returning several instances from one invocation gives all of them the same value.
   * @deprecated - As of TestNG <code>v7.13.0</code>. Use {@link IFactoryInstance#getIndex()}, which
   *     identifies the instance rather than the invocation.
   */
  @Deprecated
  int getIndex();

  /** @return - The parameters associated with the factory method as an array. */
  Object[] getParameters();

  /**
   * @return - The public view of the factory instance this object describes, or <code>null</code>
   *     for an implementation that does not provide one. Reading it never instantiates a lazy
   *     instance.
   */
  default @Nullable IFactoryInstance getFactoryInstance() {
    return null;
  }

  /**
   * @return - The class of the instance produced by the factory, known <em>without</em> having to
   *     instantiate the instance. For eager implementations this simply reflects over the (already
   *     created) instance; lazy implementations know it up-front (the declaring class of a
   *     constructor factory) and can answer without triggering construction.
   */
  default @Nullable Class<?> getTargetClass() {
    Object instance = getInstance();
    return instance == null ? null : instance.getClass();
  }

  /**
   * @return - {@code true} if this {@link IParameterInfo} defers creation of its instance until the
   *     first time {@link #getInstance()} is called. Setup-time code paths use this to avoid
   *     accidentally instantiating a lazy instance before its test is about to run.
   */
  default boolean isLazilyInitialized() {
    return false;
  }

  /**
   * @return - {@code true} if the backing instance has already been created. Always {@code true}
   *     for eager implementations; for lazy ones it flips to {@code true} once {@link
   *     #getInstance()} has instantiated (and memoized) the object.
   */
  default boolean isInstanceInstantiated() {
    return true;
  }

  /**
   * @return - The throwable raised while (lazily) constructing the instance, or {@code null} if
   *     construction has not been attempted or succeeded. Lets callers localize a lazy constructor
   *     failure to the affected instance's methods without the failure being re-thrown on every
   *     access. Always {@code null} for eager implementations.
   */
  default @Nullable Throwable getInstantiationFailure() {
    return null;
  }

  static @Nullable Object embeddedInstance(Object original) {
    if (original instanceof IParameterInfo) {
      return ((IParameterInfo) original).getInstance();
    }
    return original;
  }
}
