package org.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Allows customization of the {@link Injector} creation when working with dependency injection.
 */
@FunctionalInterface
public interface IInjectorFactory {

  /**
   * @param stage - A {@link Stage} object that defines the appropriate stage
   * @param modules - An array of {@link Module}
   * @return - An {@link com.google.inject.Inject} instance that can be used to perform dependency
   * injection.
   */
  Injector getInjector(Stage stage, Module... modules);

}
