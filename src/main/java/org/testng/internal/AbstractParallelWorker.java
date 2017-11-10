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
        return new ClassBasedParallelParallelWorker();
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

        public List<ITestNGMethod> getMethods() {
            return Collections.unmodifiableList(methods);
        }

        public void setMethods(List<ITestNGMethod> methods) {
            this.methods = methods;
        }

        public IInvoker getInvoker() {
            return invoker;
        }

        public void setInvoker(IInvoker invoker) {
            this.invoker = invoker;
        }

        public ConfigurationGroupMethods getConfigMethods() {
            return configMethods;
        }

        public void setConfigMethods(ConfigurationGroupMethods configMethods) {
            this.configMethods = configMethods;
        }

        public ClassMethodMap getClassMethodMap() {
            return classMethodMap;
        }

        public void setClassMethodMap(ClassMethodMap classMethodMap) {
            this.classMethodMap = classMethodMap;
        }

        List<IClassListener> getListeners() {
            return Collections.unmodifiableList(listeners);
        }

        public void setListeners(Collection<IClassListener> listeners) {
            this.listeners = Lists.newLinkedList(listeners);
        }

        public ITestContext getTestContext() {
            return testContext;
        }

        public void setTestContext(ITestContext testContext) {
            this.testContext = testContext;
        }

        public IAnnotationFinder getFinder() {
            return finder;
        }

        public void setFinder(IAnnotationFinder finder) {
            this.finder = finder;
        }
    }
}
