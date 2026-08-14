package org.testng;

/**
 * The <code>&#64;Factory</code> that produced a test class instance.
 *
 * <p>Reached through {@link IFactoryInstance#getFactory()}. The members below identify the factory;
 * none of them requires the produced instance to exist, so they can be read while a lazy factory
 * instance is still uninstantiated.
 *
 * @since 7.13.0
 */
public interface IFactory {

  /**
   * Returns the name of the factory.
   *
   * @return - For a factory method, the method name; for a <code>&#64;Factory</code> annotated
   *     constructor, the constructor's name, which the JDK reports as the fully qualified name of
   *     {@link #getDeclaringClass()}.
   */
  String getName();

  /**
   * Returns the class that declares the factory.
   *
   * @return - The class the <code>&#64;Factory</code> method or constructor is declared on.
   */
  Class<?> getDeclaringClass();

  /**
   * Tells whether this factory creates its instances lazily.
   *
   * @return - <code>true</code> if the instances are created just-in-time, right before the first
   *     configuration or test method of each one runs, rather than up-front during test collection.
   * @see org.testng.annotations.Factory#lazy()
   */
  boolean isLazy();
}
