package test.factory.github1631;

import org.testng.IAnnotationTransformer2;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class DataProviderTransformer implements IAnnotationTransformer2 {

    private Class<?> dataProviderClass;

    @Override
    public void transform(final IConfigurationAnnotation annotation, final Class testClass,
                          final Constructor testConstructor, final Method testMethod) {
        // not implemented
    }

    @Override
    public void transform(final IDataProviderAnnotation annotation, final Method testMethod) {
        // not implemented
    }

    @Override
    public void transform(final ITestAnnotation annotation, final Class testClass,
                          final Constructor testConstructor, final Method testMethod) {
        // not implemented
    }

    @Override
    public void transform(final IFactoryAnnotation annotation, final Method testMethod) {
        dataProviderClass = annotation.getDataProviderClass();
    }

    public Class<?> getDataProviderClass() {
        return dataProviderClass;
    }
}
