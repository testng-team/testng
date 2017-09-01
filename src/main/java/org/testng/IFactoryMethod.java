package org.testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Represents a Factory method that was involved for producing test class instances.
 */
public interface IFactoryMethod {

    /**
     * @return - The class to which the factory method belongs to.
     */
    Class<?> getDeclaringClass();

    /**
     * @return - The name of the factory method. Could either be the constructor's name or the static factory
     * method's name.
     */
    String getName();

    /**
     * @return - An array of parameters that are part of the factory method.
     */
    Class[] getParameterTypes();

    /**
     * @return - The <code>@Factory</code> annotated method.
     */
    Method getMethod();

    /**
     * @return - The <code>@Factory</code> annotated constructor.
     */
    Constructor getConstructor();

    /**
     * @return - <code>true</code> if the factory method is enabled.
     */
    boolean getEnabled();
}
