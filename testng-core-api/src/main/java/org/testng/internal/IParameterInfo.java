package org.testng.internal;

/** Represents the ability to retrieve the parameters associated with a factory method. */
public interface IParameterInfo {

  /** @return - The actual instance associated with a factory method */
  Object getInstance();

  /** @return - The actual index of instance associated with a factory method */
  int getIndex();

  /** @return - The parameters associated with the factory method as an array. */
  Object[] getParameters();

  /**
   * @return - The class of the instance produced by the factory, known <em>without</em> having to
   *     materialize the instance. For eager implementations this simply reflects over the (already
   *     created) instance; lazy implementations know it up-front (the declaring class of a
   *     constructor factory) and can answer without triggering construction.
   */
  default Class<?> getTargetClass() {
    Object instance = getInstance();
    return instance == null ? null : instance.getClass();
  }

  /**
   * @return - {@code true} if this {@link IParameterInfo} defers creation of its instance until the
   *     first time {@link #getInstance()} is called. Setup-time code paths use this to avoid
   *     accidentally materializing a lazy instance before its test is about to run.
   */
  default boolean isLazilyInitialized() {
    return false;
  }

  /**
   * @return - {@code true} if the backing instance has already been created. Always {@code true}
   *     for eager implementations; for lazy ones it flips to {@code true} once {@link
   *     #getInstance()} has materialized (and memoized) the object.
   */
  default boolean isInstanceMaterialized() {
    return true;
  }

  /**
   * @return - The throwable raised while (lazily) constructing the instance, or {@code null} if
   *     construction has not been attempted or succeeded. Lets callers localize a lazy constructor
   *     failure to the affected instance's methods without the failure being re-thrown on every
   *     access. Always {@code null} for eager implementations.
   */
  default Throwable getMaterializationFailure() {
    return null;
  }

  static Object embeddedInstance(Object original) {
    if (original instanceof IParameterInfo) {
      return ((IParameterInfo) original).getInstance();
    }
    return original;
  }
}
