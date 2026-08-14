package org.testng.internal;

/**
 * Represents the ability to retrieve the parameters associated with a factory method.
 *
 * @deprecated - This interface stands deprecated as of TestNG <code>7.11.0</code>.
 */
@Deprecated
public interface IParameterInfo extends org.testng.IParameterInfo {

  /**
   * @return - The class of the instance produced by the factory, known <em>without</em> having to
   *     instantiate the instance. For eager implementations this simply reflects over the (already
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
  default Throwable getInstantiationFailure() {
    return null;
  }

  static Object embeddedInstance(Object original) {
    return org.testng.IParameterInfo.embeddedInstance(original);
  }
}
