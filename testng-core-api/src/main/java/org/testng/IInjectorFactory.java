package org.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import javax.annotation.Nullable;

/** Allows customization of the {@link Injector} creation when working with dependency injection. */
public interface IInjectorFactory {
  /**
   * Note that {@link #getInjector(Injector, Stage, Module...)} should be used instead.
   *
   * @param stage - A {@link Stage} object that defines the appropriate stage
   * @param modules - An array of {@link Module}
   * @return - An {@link com.google.inject.Injector} instance that can be used to perform dependency
   *     injection.
   * @deprecated - As of TestNG <code>7.5.0</code>
   */
  @Deprecated
  Injector getInjector(Stage stage, Module... modules);

  /**
   * Adding this method breaks existing implementations therefore for the time being (until
   * deprecated method is removed) it calls the existing method.
   *
   * @param parent - Parent {@link com.google.inject.Injector} instance that was built with parent
   *     injector
   * @param stage - A {@link Stage} object that defines the appropriate stage
   * @param modules - An array of {@link Module}
   * @return - An {@link com.google.inject.Injector} instance that can be used to perform dependency
   *     injection.
   */
  default Injector getInjector(@Nullable Injector parent, Stage stage, Module... modules) {
    return getInjector(stage, modules);
  }
}
