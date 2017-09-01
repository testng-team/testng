package org.testng.internal;

import com.google.inject.Injector;
import org.testng.IDataProviderMethod;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNGException;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IDataProvidable;
import org.testng.util.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A Helper class that houses utilities related to data providers.
 */
class DataProviderHelper {
    private DataProviderHelper() {
        //Utility class. Defeat instantiation.
    }

    static IDataProviderMethod findDataProvider(Object instance, ITestNGMethod testMethod,
                                                IAnnotationFinder finder, ITestContext context) {
        ITestClass clazz = testMethod.getTestClass();
        ConstructorOrMethod m = testMethod.getConstructorOrMethod();
        IDataProviderMethod result = null;

        IDataProvidable dp = findDataProviderInfo(clazz, m, finder);
        if (dp == null) {
            return null;
        }
        String dataProviderName = dp.getDataProvider();
        Class dataProviderClass = dp.getDataProviderClass();

        if (!Utils.isStringEmpty(dataProviderName)) {
            result = findDataProvider(instance, testMethod, finder, dataProviderName, dataProviderClass, context);

            if (null == result) {
                throw new TestNGException("Method " + m + " requires a @DataProvider named : "
                        + dataProviderName + (dataProviderClass != null ? " in class " + dataProviderClass.getName() : "")
                );
            }
        }

        return result;
    }

    /**
     * Find the data provider info (data provider name and class) on either <code>@Test(dataProvider)</code>,
     * <p>
     * <code>@Factory(dataProvider)</code> on a method or <code>@Factory(dataProvider)</code> on a constructor.
     */
    private static IDataProvidable findDataProviderInfo(ITestClass clazz, ConstructorOrMethod m,
                                                        IAnnotationFinder finder) {
        IDataProvidable result;

        if (m.getMethod() == null) {
            //@Factory(dataProvider) on a constructor
            return AnnotationHelper.findFactory(finder, m.getConstructor());
        }

        //
        // @Test(dataProvider) on a method
        //
        result = AnnotationHelper.findTest(finder, m.getMethod());
        if (result == null) {
            //
            // @Factory(dataProvider) on a method
            //
            result = AnnotationHelper.findFactory(finder, m.getMethod());
        }
        if (result == null) {
            //
            // @Test(dataProvider) on a class
            result = AnnotationHelper.findTest(finder, clazz.getRealClass());
        }

        return result;
    }

    /**
     * Find a method that has a @DataProvider(name=name)
     */
    private static IDataProviderMethod findDataProvider(Object instance, ITestNGMethod testNGMethod,
                                                        IAnnotationFinder finder,
                                                        String name, Class<?> dataProviderClass,
                                                        ITestContext context) {
        IDataProviderMethod result = null;
        ITestClass clazz = testNGMethod.getTestClass();

        Class<?> cls = clazz.getRealClass();
        boolean shouldBeStatic = false;
        if (dataProviderClass != null) {
            cls = dataProviderClass;
            shouldBeStatic = true;
        }

        for (Method m : ClassHelper.getAvailableMethods(cls)) {
            IDataProviderAnnotation dp = finder.findAnnotation(m, IDataProviderAnnotation.class);
            if (null != dp && name.equals(getDataProviderName(dp, m))) {
                Object instanceToUse;
                if (shouldBeStatic && (m.getModifiers() & Modifier.STATIC) == 0) {
                    Injector injector = context.getInjector(clazz);
                    if (injector != null) {
                        instanceToUse = injector.getInstance(dataProviderClass);
                    } else {
                        instanceToUse = ClassHelper.newInstance(dataProviderClass);
                    }
                } else {
                    instanceToUse = instance;
                }
                // Not a static method but no instance exists, then create new one if possible
                if ((m.getModifiers() & Modifier.STATIC) == 0 && instanceToUse == null) {
                    instanceToUse = ClassHelper.newInstanceOrNull(cls);
                }

                if (result != null) {
                    throw new TestNGException("Found two providers called '" + name + "' on " + cls);
                }
                result = new DataProviderMethod(instanceToUse, m, dp, testNGMethod.getConstructorOrMethod());
            }
        }

        return result;
    }

    private static String getDataProviderName(IDataProviderAnnotation dp, Method m) {
        return Strings.isNullOrEmpty(dp.getName()) ? m.getName() : dp.getName();
    }
}
