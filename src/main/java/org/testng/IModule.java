package org.testng;

import com.google.inject.Module;

/**
 * This is a marker interface for a Guice module to be instantiated with {@link java.util.ServiceLoader}.
 *
 * @author Bartosz Popiela
 */
public interface IModule extends Module {
}