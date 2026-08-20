package org.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.jspecify.annotations.Nullable;

/** Allows customization of the {@link Injector} creation when working with dependency injection. */
public interface IInjectorFactory {

  /**
   * The factory {@link org.testng.ITestContext#getInjectorFactory()} answers for a context that
   * names none.
   *
   * <p>A factory that may be absent forces every caller down the Guice path to decide what an
   * absent one means, and each of them decided the same thing: that it cannot happen. One shared
   * token keeps the answer present, and a suite that reaches it has declared no factory and no
   * {@code @Guice} class either, so nothing ever asks it for an injector.
   */
  IInjectorFactory NONE = new IInjectorFactory() {};

  /**
   * @param parent - Parent {@link com.google.inject.Injector} instance that was built with parent
   *     injector
   * @param stage - A {@link Stage} object that defines the appropriate stage
   * @param modules - An array of {@link Module}
   * @return - An {@link com.google.inject.Injector} instance that can be used to perform dependency
   *     injection.
   */
  default Injector getInjector(@Nullable Injector parent, Stage stage, Module... modules) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
