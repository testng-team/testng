package org.testng;

/**
 * The <code>@Factory</code> that produced a test class instance.
 *
 * <p>Reached through {@link IFactoryInstance#getFactory()}. The members below identify the factory;
 * none of them requires the produced instance to exist, so they can be read while a lazy factory
 * instance is still uninstantiated.
 *
 * @since 7.13.0
 */
public interface IFactory {

  /**
   * @return - The name of the factory. For a factory method that is the method name; for a <code>
   *     @Factory</code> annotated constructor it is the constructor's name, which the JDK reports
   *     as the fully qualified name of {@link #getDeclaringClass()}.
   */
  String getName();

  /** @return - The class that declares the <code>@Factory</code> method or constructor. */
  Class<?> getDeclaringClass();

  /**
   * @return - <code>true</code> if this factory creates its instances just-in-time, right before
   *     the first configuration or test method of each instance runs, rather than up-front during
   *     test collection.
   * @see org.testng.annotations.Factory#lazy()
   */
  boolean isLazy();
}
