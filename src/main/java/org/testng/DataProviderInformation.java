package org.testng;

import java.lang.reflect.Method;

/**
 * Represents information pertaining to a data provider that would be invoked.
 */
public class DataProviderInformation {
    private final Object instance;
    private final Method method;

    public DataProviderInformation(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    /**
     * @return - The instance to which the data provider belongs to. <code>null</code> if the data provider
     * is a static one.
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * @return - A {@link Method} object that represents the actual
     * {@literal @}{@link org.testng.annotations.DataProvider} method.
     */
    public Method getMethod() {
        return method;
    }

}
