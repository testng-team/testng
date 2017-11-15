package org.testng.internal.annotations;

import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Ignore;
import org.testng.util.Strings;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

public class IgnoreListener implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (!annotation.getEnabled()) {
            return;
        }
        Class<?> typedTestClass = testClass;
        if (testMethod != null) {
            ignoreTest(annotation, testMethod.getAnnotation(Ignore.class));
            typedTestClass = testMethod.getDeclaringClass();
        }
        if (typedTestClass != null) {
            ignoreTest(annotation, typedTestClass.getAnnotation(Ignore.class));
            Package testPackage = typedTestClass.getPackage();
            if (testPackage != null) {
                ignoreTest(annotation, findAnnotation(testPackage));
            }
        }
    }

    private static void ignoreTest(ITestAnnotation annotation, Ignore ignore) {
        if (ignore == null) {
            return;
        }
        annotation.setEnabled(false);
        updateDescription(annotation, ignore);
    }

    private static void updateDescription(ITestAnnotation annotation, Ignore ignore) {
        if (ignore.value().isEmpty()) {
            return;
        }
        String ignoredDescription;
        if (annotation.getDescription() == null || annotation.getDescription().isEmpty()) {
            ignoredDescription = ignore.value();
        } else {
            ignoredDescription = ignore.value() + ": " + annotation.getDescription();
        }
        annotation.setDescription(ignoredDescription);
    }

    private static Ignore findAnnotation(Package testPackage) {
        if (testPackage == null) {
            return null;
        }
        Ignore result = testPackage.getAnnotation(Ignore.class);
        if (result != null) {
            return result;
        }
        String[] parts = testPackage.getName().split("\\.");
        String[] parentParts = Arrays.copyOf(parts, parts.length - 1);
        String parentPackageName = Strings.join(".", parentParts);
        if (parentPackageName.isEmpty()) {
            return null;
        }
        return findAnnotation(Package.getPackage(parentPackageName));
    }
}