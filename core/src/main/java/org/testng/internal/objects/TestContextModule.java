package org.testng.internal.objects;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.testng.ITestContext;
import org.testng.annotations.CurrentTestClass;

/**
 * Provides default Guice bindings so test code could inject {@link ITestContext}
 * and {@code testCLass} if needed.
 */
public class TestContextModule extends AbstractModule {
    private final ITestContext context;
    private final Class<?> testClass;

    public TestContextModule(ITestContext context, Class<?> testClass) {
        this.context = context;
        this.testClass = testClass;
    }

    @Override
    protected void configure() {
        // Guice does not allow null bindings, and sometimes context is null
        // For instance, test.guice.issue279.IssueTest.classWithModuleDefinedInSuite
        if (context != null) {
            bind(ITestContext.class).toInstance(context);
        }
        bind(Class.class).annotatedWith(CurrentTestClass.class).toInstance(testClass);
        bind(new TypeLiteral<Class<?>>() {
        })
                .annotatedWith(CurrentTestClass.class).toInstance(testClass);
    }
}
