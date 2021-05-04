/*
 * Copyright (C) 2010-2021 Evergage, Inc.
 * All rights reserved.
 */

package test.github2360;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A unit test for https://github.com/cbeust/testng/issues/2360.
 */
@Test(description = "GITHUB-2360")
public class Issue2360Test extends SimpleBaseTest {

    public void testGroovyInternalMethodsAreSkipped() {
        TestNG tng = create(Issue2360Sample.class);
        AtomicReference<List<IMethodInstance>> testMethods = new AtomicReference<>();
        IMethodInterceptor methodInterceptor = (methods, context) -> {
            testMethods.set(methods);
            return methods;
        };
        tng.setMethodInterceptor(methodInterceptor);
        tng.run();

        List<String> testMethodNames = testMethods.get().stream()
                .map(method -> method.getMethod().getMethodName())
                .collect(Collectors.toList());
        assertThat(testMethodNames).containsExactly("test1", "test2");
    }

}
