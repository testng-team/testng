package org.testng.internal;

import org.testng.IDataProviderMethod;
import org.testng.IFactoryMethod;
import org.testng.annotations.IDataProviderAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents an @{@link org.testng.annotations.DataProvider} annotated method.
 */
class DataProviderMethod implements IDataProviderMethod {

    private final Object instance;
    private final Method method;
    private final IDataProviderAnnotation annotation;
    private final IFactoryMethod factoryMethod;

    DataProviderMethod(Object instance, Method method, IDataProviderAnnotation annotation, IFactoryMethod factoryMethod) {
        this.instance = instance;
        this.method = method;
        this.annotation = annotation;
        this.factoryMethod = factoryMethod;
    }

    @Override
    public Object getInstance() {
        return instance;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String getName() {
        return annotation.getName();
    }

    @Override
    public boolean isParallel() {
        return annotation.isParallel();
    }

    @Override
    public List<Integer> getIndices() {
        return annotation.getIndices();
    }

    @Override
    public IFactoryMethod getFactoryMethod() {
        return factoryMethod;
    }
}
