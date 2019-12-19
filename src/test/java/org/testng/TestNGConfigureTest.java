package org.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class TestNGConfigureTest {

    @Test(description = "GITHUB-2207")
    public void testInjectorFactoryCanBeConfiguredViaProperties() {
        Map<String, String> params = new HashMap<>();
        params.put(CommandLineArgs.DEPENDENCY_INJECTOR_FACTORY, TestInjectorFactory.class.getName());
        TestNG testNG = new TestNG();
        testNG.configure(params);

        IInjectorFactory resolvedInjectorFactory = testNG.getConfiguration().getInjectorFactory();
        Assert.assertEquals(resolvedInjectorFactory.getClass(), TestInjectorFactory.class);

    }

    public static class TestInjectorFactory implements IInjectorFactory {

        @Override
        public Injector getInjector(Stage stage, Module... modules) {
            return null;
        }
    }
}
