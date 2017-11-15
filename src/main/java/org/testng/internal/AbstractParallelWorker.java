package org.testng.internal;

import org.testng.ClassMethodMap;
import org.testng.IClassListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.collections.Lists;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlSuite;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractParallelWorker {

    public static AbstractParallelWorker newWorker(XmlSuite.ParallelMode mode) {
        if (XmlSuite.ParallelMode.INSTANCES.equals(mode)) {
            return new InstanceBasedParallelParallelWorker();
        }
        return new ClassBasedParallelWorker();
    }

    public abstract List<IWorker<ITestNGMethod>> createWorkers(Arguments arguments);

    public static class Arguments {
        private List<ITestNGMethod> methods;
        private IInvoker invoker;
        private ConfigurationGroupMethods configMethods;
        private ClassMethodMap classMethodMap;
        private List<IClassListener> listeners;
        private ITestContext testContext;
        private IAnnotationFinder finder;

        private Arguments() {
            //We have a builder. Defeat instantiation via constructors.
        }

        public List<ITestNGMethod> getMethods() {
            return Collections.unmodifiableList(methods);
        }

        public IInvoker getInvoker() {
            return invoker;
        }

        public ConfigurationGroupMethods getConfigMethods() {
            return configMethods;
        }

        public ClassMethodMap getClassMethodMap() {
            return classMethodMap;
        }

        List<IClassListener> getListeners() {
            return Collections.unmodifiableList(listeners);
        }

        public ITestContext getTestContext() {
            return testContext;
        }

        public IAnnotationFinder getFinder() {
            return finder;
        }

        public static class Builder {
            private Arguments instance;

            public Builder() {
                instance = new Arguments();
            }

            public Builder methods(List<ITestNGMethod> methods) {
                instance.methods = methods;
                return this;
            }

            public Builder invoker(IInvoker invoker) {
                instance.invoker = invoker;
                return this;
            }

            public Builder configMethods(ConfigurationGroupMethods configMethods) {
                instance.configMethods = configMethods;
                return this;
            }

            public Builder classMethodMap(ClassMethodMap classMethodMap) {
                instance.classMethodMap = classMethodMap;
                return this;
            }

            public Builder listeners(Collection<IClassListener> listeners) {
                instance.listeners = Lists.newLinkedList(listeners);
                return this;
            }

            public Builder testContext(ITestContext testContext) {
                instance.testContext = testContext;
                return this;
            }

            public Builder finder(IAnnotationFinder finder) {
                instance.finder = finder;
                return this;
            }

            public Arguments build() {
                return instance;
            }
        }

    }
}
