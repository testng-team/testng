package org.testng;

import javax.annotation.Nullable;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Allows customization of the {@link Injector} creation when working with dependency injection.
 */
public interface IInjectorFactory {
  /**
   * @param stage - A {@link Stage} object that defines the appropriate stage
   * @param modules - An array of {@link Module}
   * @return - An {@link com.google.inject.Injector} instance that can be used to perform dependency
   * injection.
   */
  @Deprecated
  default Injector getInjector(Stage stage, Module... modules) {
    return getInjector(null, stage, modules);
  }

  /**
   * @param parent - Parent {@link com.google.inject.Injector} instance that was built with parent injector
   * @param stage - A {@link Stage} object that defines the appropriate stage
   * @param modules - An array of {@link Module}
   * @return - An {@link com.google.inject.Injector} instance that can be used to perform dependency
   * injection.
   */
  Injector getInjector(@Nullable Injector parent, Stage stage, Module... modules);
}
