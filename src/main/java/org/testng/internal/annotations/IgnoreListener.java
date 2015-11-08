package org.testng.internal.annotations;

import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Ignore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class IgnoreListener implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (testMethod != null) {
            ignoreTest(annotation, testMethod.getAnnotation(Ignore.class));
            if (annotation.getEnabled()) {
                ignoreTest(annotation, testMethod.getDeclaringClass().getAnnotation(Ignore.class));
            }
        }
    }

    private static void ignoreTest(ITestAnnotation annotation, Ignore ignore) {
        if (ignore != null) {
            annotation.setEnabled(false);
            if (!ignore.value().isEmpty()) {
                String ignoredDescription;
                if (annotation.getDescription() == null || annotation.getDescription().isEmpty()) {
                    ignoredDescription = ignore.value();
                } else {
                    ignoredDescription = ignore.value() + ": " + annotation.getDescription();
                }
                annotation.setDescription(ignoredDescription);
            }
        }
    }
}