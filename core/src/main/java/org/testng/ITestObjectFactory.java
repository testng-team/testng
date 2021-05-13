package org.testng;

import org.testng.internal.objects.InstanceCreator;

import java.lang.reflect.Constructor;

/**
 * Parent interface of all the object factories.
 */
public interface ITestObjectFactory {

    default <T> T newInstance(Class<T> cls, Object... parameters) {
        return InstanceCreator.newInstance(cls, parameters);
    }

    default <T> T newInstance(String clsName, Object... parameters) {
        return InstanceCreator.newInstance(clsName, parameters);
    }

    default <T> T newInstance(Constructor<T> constructor, Object... parameters) {
        return InstanceCreator.newInstance(constructor, parameters);
    }
}
