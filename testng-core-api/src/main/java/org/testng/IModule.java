package org.testng;

import com.google.inject.Module;

/**
 * This interface provides {@link Module} to implicitly add to the Guice context. Classes that
 * implement this interface are instantiated with {@link java.util.ServiceLoader ServiceLoader}.
 */
public interface IModule {
  Module getModule();
}
